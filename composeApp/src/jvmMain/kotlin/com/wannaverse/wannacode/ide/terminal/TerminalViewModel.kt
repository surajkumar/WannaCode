package com.wannaverse.wannacode.ide.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TerminalSession(
    val id: Int,
    val name: String,
    val workingDirectory: File
)

data class TerminalChar(
    val char: Char,
    val foreground: Color = Color.White,
    val background: Color = Color.Transparent,
    val bold: Boolean = false
)

data class TerminalLine(
    val chars: MutableList<TerminalChar> = mutableListOf()
) {
    val text: String get() = chars.map { it.char }.joinToString("")
}

class TerminalViewModel : ViewModel() {
    private var nextId = 0
    private val _sessions = mutableStateMapOf<Int, TerminalSession>()
    val sessions: Map<Int, TerminalSession> = _sessions

    private val sessionLinesMap = mutableStateMapOf<Int, SnapshotStateList<TerminalLine>>()
    private val sessionProcesses = mutableStateMapOf<Int, PtyProcess>()
    private val sessionJobs = mutableStateMapOf<Int, Job>()

    private val _cursorRow = mutableStateMapOf<Int, Int>()
    private val _cursorCol = mutableStateMapOf<Int, Int>()
    private val _terminalCols = mutableStateMapOf<Int, Int>()
    private val _terminalRows = mutableStateMapOf<Int, Int>()

    private val _currentFg = mutableStateMapOf<Int, Color>()
    private val _currentBg = mutableStateMapOf<Int, Color>()
    private val _currentBold = mutableStateMapOf<Int, Boolean>()

    var currentSessionId by mutableStateOf(-1)
        private set

    var workingDirectory by mutableStateOf<File?>(null)

    val orderedSessionIds: List<Int>
        get() = _sessions.keys.toList().sorted()

    fun getSessionLines(sessionId: Int): List<TerminalLine> {
        return sessionLinesMap[sessionId] ?: emptyList()
    }

    fun getWorkingDir(sessionId: Int): File {
        return workingDirectory ?: File(System.getProperty("user.home"))
    }

    fun createSession(name: String = "Terminal"): Int {
        val id = nextId++
        val dir = workingDirectory ?: File(System.getProperty("user.home"))
        val session = TerminalSession(id, "$name ${id + 1}", dir)
        _sessions[id] = session
        sessionLinesMap[id] = mutableStateListOf()

        _cursorRow[id] = 0
        _cursorCol[id] = 0
        _terminalCols[id] = 120
        _terminalRows[id] = 24
        _currentFg[id] = Color.White
        _currentBg[id] = Color.Transparent
        _currentBold[id] = false

        currentSessionId = id

        startPtyProcess(id, dir)

        return id
    }

    private fun startPtyProcess(
        sessionId: Int,
        workDir: File,
        extraEnv: Map<String, String> = emptyMap()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cols = _terminalCols[sessionId] ?: 120
                val rows = _terminalRows[sessionId] ?: 24

                val env = System.getenv().toMutableMap()
                env["TERM"] = "xterm-256color"
                env["COLORTERM"] = "truecolor"
                env.putAll(extraEnv)

                val shell = if (System.getProperty("os.name").lowercase().contains("win")) {
                    arrayOf("cmd.exe")
                } else {
                    val userShell = System.getenv("SHELL") ?: "/bin/bash"
                    arrayOf(userShell)
                }

                val ptyProcess = PtyProcessBuilder()
                    .setCommand(shell)
                    .setDirectory(workDir.absolutePath)
                    .setEnvironment(env)
                    .setInitialColumns(cols)
                    .setInitialRows(rows)
                    .setConsole(false)
                    .start()

                sessionProcesses[sessionId] = ptyProcess

                sessionJobs[sessionId] = viewModelScope.launch(Dispatchers.IO) {
                    val inputStream = ptyProcess.inputStream
                    val buffer = ByteArray(4096)

                    try {
                        while (isActive && ptyProcess.isAlive) {
                            val bytesRead = inputStream.read(buffer)
                            if (bytesRead > 0) {
                                val text = String(buffer, 0, bytesRead, StandardCharsets.UTF_8)
                                withContext(Dispatchers.Main) {
                                    processOutput(sessionId, text)
                                }
                            } else if (bytesRead == -1) {
                                // End of stream
                                break
                            }
                        }
                    } catch (e: IOException) {
                        // Stream closed
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    appendText(sessionId, "Error starting terminal: ${e.message}\n")
                }
            }
        }
    }

    fun sendInput(sessionId: Int, text: String) {
        val process = sessionProcesses[sessionId] ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                process.outputStream.write(text.toByteArray(StandardCharsets.UTF_8))
                process.outputStream.flush()
            } catch (e: IOException) {
                // Process might have closed
            }
        }
    }

    fun sendKey(sessionId: Int, key: TerminalKey) {
        val sequence = when (key) {
            TerminalKey.ENTER -> "\r"
            TerminalKey.TAB -> "\t"
            TerminalKey.BACKSPACE -> "\u007F"
            TerminalKey.ESCAPE -> "\u001B"
            TerminalKey.UP -> "\u001B[A"
            TerminalKey.DOWN -> "\u001B[B"
            TerminalKey.RIGHT -> "\u001B[C"
            TerminalKey.LEFT -> "\u001B[D"
            TerminalKey.HOME -> "\u001B[H"
            TerminalKey.END -> "\u001B[F"
            TerminalKey.PAGE_UP -> "\u001B[5~"
            TerminalKey.PAGE_DOWN -> "\u001B[6~"
            TerminalKey.INSERT -> "\u001B[2~"
            TerminalKey.DELETE -> "\u001B[3~"
            TerminalKey.F1 -> "\u001BOP"
            TerminalKey.F2 -> "\u001BOQ"
            TerminalKey.F3 -> "\u001BOR"
            TerminalKey.F4 -> "\u001BOS"
            TerminalKey.F5 -> "\u001B[15~"
            TerminalKey.F6 -> "\u001B[17~"
            TerminalKey.F7 -> "\u001B[18~"
            TerminalKey.F8 -> "\u001B[19~"
            TerminalKey.F9 -> "\u001B[20~"
            TerminalKey.F10 -> "\u001B[21~"
            TerminalKey.F11 -> "\u001B[23~"
            TerminalKey.F12 -> "\u001B[24~"
        }
        sendInput(sessionId, sequence)
    }

    fun sendControlKey(sessionId: Int, char: Char) {
        val controlCode = (char.uppercaseChar().code - 'A'.code + 1).toChar()
        sendInput(sessionId, controlCode.toString())
    }

    fun resizeTerminal(sessionId: Int, cols: Int, rows: Int) {
        _terminalCols[sessionId] = cols
        _terminalRows[sessionId] = rows
        val process = sessionProcesses[sessionId] ?: return
        try {
            process.winSize = com.pty4j.WinSize(cols, rows)
        } catch (e: Exception) {
            // Ignore resize errors
        }
    }

    private fun processOutput(sessionId: Int, text: String) {
        var i = 0
        while (i < text.length) {
            val char = text[i]

            if (char == '\u001B' && i + 1 < text.length) {
                val escapeResult = parseEscapeSequence(sessionId, text, i)
                i = escapeResult
            } else {
                when (char) {
                    '\n' -> newLine(sessionId)
                    '\r' -> carriageReturn(sessionId)
                    '\b' -> backspace(sessionId)
                    '\t' -> tab(sessionId)
                    '\u0007' -> { /* Bell - ignore */ }
                    else -> {
                        if (char.code >= 32 || char == '\t') {
                            appendChar(sessionId, char)
                        }
                    }
                }
                i++
            }
        }
    }

    private fun parseEscapeSequence(sessionId: Int, text: String, startIndex: Int): Int {
        if (startIndex + 1 >= text.length) return startIndex + 1

        val nextChar = text[startIndex + 1]

        return when (nextChar) {
            '[' -> parseCSISequence(sessionId, text, startIndex + 2)
            ']' -> parseOSCSequence(text, startIndex + 2)
            '(' -> startIndex + 3 // Skip charset designation
            ')' -> startIndex + 3
            '>' -> startIndex + 2 // Normal keypad mode
            '=' -> startIndex + 2 // Application keypad mode
            'M' -> { scrollUp(sessionId); startIndex + 2 }
            'D' -> { scrollDown(sessionId); startIndex + 2 }
            'E' -> { newLine(sessionId); startIndex + 2 }
            '7' -> startIndex + 2 // Save cursor (ignore for now)
            '8' -> startIndex + 2 // Restore cursor (ignore for now)
            else -> startIndex + 2
        }
    }

    private fun parseCSISequence(sessionId: Int, text: String, startIndex: Int): Int {
        var i = startIndex
        val params = StringBuilder()

        while (i < text.length) {
            val char = text[i]
            when {
                char.isDigit() || char == ';' || char == '?' -> {
                    params.append(char)
                    i++
                }
                char.isLetter() || char == '@' || char == '`' -> {
                    handleCSICommand(sessionId, char, params.toString())
                    return i + 1
                }
                else -> return i + 1
            }
        }
        return i
    }

    private fun parseOSCSequence(text: String, startIndex: Int): Int {
        var i = startIndex
        while (i < text.length) {
            if (text[i] == '\u0007') return i + 1
            if (i + 1 < text.length && text[i] == '\u001B' && text[i + 1] == '\\') return i + 2
            i++
        }
        return i
    }

    private fun handleCSICommand(sessionId: Int, command: Char, params: String) {
        val parts = params.replace("?", "").split(";").map { it.toIntOrNull() ?: 0 }
        val p1 = parts.getOrElse(0) { 0 }
        val p2 = parts.getOrElse(1) { 0 }

        when (command) {
            'A' -> moveCursorUp(sessionId, maxOf(1, p1))
            'B' -> moveCursorDown(sessionId, maxOf(1, p1))
            'C' -> moveCursorRight(sessionId, maxOf(1, p1))
            'D' -> moveCursorLeft(sessionId, maxOf(1, p1))
            'E' -> { moveCursorDown(sessionId, maxOf(1, p1)); carriageReturn(sessionId) }
            'F' -> { moveCursorUp(sessionId, maxOf(1, p1)); carriageReturn(sessionId) }
            'G' -> setCursorCol(sessionId, maxOf(1, p1) - 1)
            'H', 'f' -> setCursorPosition(sessionId, maxOf(1, p1) - 1, maxOf(1, p2) - 1)
            'J' -> eraseDisplay(sessionId, p1)
            'K' -> eraseLine(sessionId, p1)
            'L' -> insertLines(sessionId, maxOf(1, p1))
            'M' -> deleteLines(sessionId, maxOf(1, p1))
            'P' -> deleteChars(sessionId, maxOf(1, p1))
            'S' -> scrollUp(sessionId, maxOf(1, p1))
            'T' -> scrollDown(sessionId, maxOf(1, p1))
            'X' -> eraseChars(sessionId, maxOf(1, p1))
            'd' -> setCursorRow(sessionId, maxOf(1, p1) - 1)
            'm' -> handleSGR(sessionId, parts)
            'r' -> { /* Set scrolling region - ignore for now */ }
            's' -> { /* Save cursor position */ }
            'u' -> { /* Restore cursor position */ }
            'h', 'l' -> { /* Mode setting - ignore for now */ }
            'n' -> { /* Device status report - ignore */ }
            'c' -> { /* Device attributes - ignore */ }
        }
    }

    private fun handleSGR(sessionId: Int, params: List<Int>) {
        var i = 0
        while (i < params.size) {
            when (val p = params[i]) {
                0 -> {
                    _currentFg[sessionId] = Color.White
                    _currentBg[sessionId] = Color.Transparent
                    _currentBold[sessionId] = false
                }
                1 -> _currentBold[sessionId] = true
                22 -> _currentBold[sessionId] = false
                in 30..37 -> _currentFg[sessionId] = ansi8Color(p - 30, _currentBold[sessionId] ?: false)
                38 -> {
                    if (i + 2 < params.size && params[i + 1] == 5) {
                        _currentFg[sessionId] = ansi256Color(params[i + 2])
                        i += 2
                    } else if (i + 4 < params.size && params[i + 1] == 2) {
                        _currentFg[sessionId] = Color(params[i + 2], params[i + 3], params[i + 4])
                        i += 4
                    }
                }
                39 -> _currentFg[sessionId] = Color.White
                in 40..47 -> _currentBg[sessionId] = ansi8Color(p - 40, false)
                48 -> {
                    if (i + 2 < params.size && params[i + 1] == 5) {
                        _currentBg[sessionId] = ansi256Color(params[i + 2])
                        i += 2
                    } else if (i + 4 < params.size && params[i + 1] == 2) {
                        _currentBg[sessionId] = Color(params[i + 2], params[i + 3], params[i + 4])
                        i += 4
                    }
                }
                49 -> _currentBg[sessionId] = Color.Transparent
                in 90..97 -> _currentFg[sessionId] = ansi8Color(p - 90, true)
                in 100..107 -> _currentBg[sessionId] = ansi8Color(p - 100, true)
            }
            i++
        }
    }

    private fun ansi8Color(index: Int, bright: Boolean): Color {
        val colors = if (bright) {
            listOf(
                Color(0xFF, 0x55, 0x55), Color(0x55, 0xFF, 0x55), Color(0xFF, 0xFF, 0x55), Color(0x55, 0x55, 0xFF),
                Color(0xFF, 0x55, 0xFF), Color(0x55, 0xFF, 0xFF), Color(0xFF, 0xFF, 0xFF), Color(0x55, 0x55, 0x55)
            )
        } else {
            listOf(
                Color(0x00, 0x00, 0x00), Color(0xAA, 0x00, 0x00), Color(0x00, 0xAA, 0x00), Color(0xAA, 0x55, 0x00),
                Color(0x00, 0x00, 0xAA), Color(0xAA, 0x00, 0xAA), Color(0x00, 0xAA, 0xAA), Color(0xAA, 0xAA, 0xAA)
            )
        }
        return colors.getOrElse(index) { Color.White }
    }

    private fun ansi256Color(index: Int): Color {
        return when {
            index < 16 -> ansi8Color(index % 8, index >= 8)
            index < 232 -> {
                val i = index - 16
                val r = (i / 36) * 51
                val g = ((i / 6) % 6) * 51
                val b = (i % 6) * 51
                Color(r, g, b)
            }
            else -> {
                val gray = (index - 232) * 10 + 8
                Color(gray, gray, gray)
            }
        }
    }

    private fun appendChar(sessionId: Int, char: Char) {
        val lines = sessionLinesMap[sessionId] ?: return
        val row = _cursorRow[sessionId] ?: 0
        val col = _cursorCol[sessionId] ?: 0
        val cols = _terminalCols[sessionId] ?: 120

        ensureLineExists(sessionId, row)
        val line = lines[row]

        // Extend line if needed
        while (line.chars.size <= col) {
            line.chars.add(TerminalChar(' '))
        }

        val termChar = TerminalChar(
            char = char,
            foreground = _currentFg[sessionId] ?: Color.White,
            background = _currentBg[sessionId] ?: Color.Transparent,
            bold = _currentBold[sessionId] ?: false
        )

        if (col < line.chars.size) {
            line.chars[col] = termChar
        } else {
            line.chars.add(termChar)
        }

        _cursorCol[sessionId] = col + 1

        // Auto wrap
        if ((_cursorCol[sessionId] ?: 0) >= cols) {
            _cursorCol[sessionId] = 0
            _cursorRow[sessionId] = (row + 1)
            ensureLineExists(sessionId, _cursorRow[sessionId] ?: 0)
        }

        // Trigger recomposition
        lines[row] = line.copy()
    }

    private fun appendText(sessionId: Int, text: String) {
        text.forEach { char ->
            when (char) {
                '\n' -> newLine(sessionId)
                '\r' -> carriageReturn(sessionId)
                else -> appendChar(sessionId, char)
            }
        }
    }

    private fun newLine(sessionId: Int) {
        val row = _cursorRow[sessionId] ?: 0
        val rows = _terminalRows[sessionId] ?: 24

        if (row >= rows - 1) {
            scrollUp(sessionId, 1)
        } else {
            _cursorRow[sessionId] = row + 1
        }
        ensureLineExists(sessionId, _cursorRow[sessionId] ?: 0)
    }

    private fun carriageReturn(sessionId: Int) {
        _cursorCol[sessionId] = 0
    }

    private fun backspace(sessionId: Int) {
        val col = _cursorCol[sessionId] ?: 0
        if (col > 0) {
            _cursorCol[sessionId] = col - 1
        }
    }

    private fun tab(sessionId: Int) {
        val col = _cursorCol[sessionId] ?: 0
        val nextTab = ((col / 8) + 1) * 8
        _cursorCol[sessionId] = nextTab
    }

    private fun moveCursorUp(sessionId: Int, n: Int) {
        val row = _cursorRow[sessionId] ?: 0
        _cursorRow[sessionId] = maxOf(0, row - n)
    }

    private fun moveCursorDown(sessionId: Int, n: Int) {
        val row = _cursorRow[sessionId] ?: 0
        val rows = _terminalRows[sessionId] ?: 24
        _cursorRow[sessionId] = minOf(rows - 1, row + n)
    }

    private fun moveCursorRight(sessionId: Int, n: Int) {
        val col = _cursorCol[sessionId] ?: 0
        val cols = _terminalCols[sessionId] ?: 120
        _cursorCol[sessionId] = minOf(cols - 1, col + n)
    }

    private fun moveCursorLeft(sessionId: Int, n: Int) {
        val col = _cursorCol[sessionId] ?: 0
        _cursorCol[sessionId] = maxOf(0, col - n)
    }

    private fun setCursorPosition(sessionId: Int, row: Int, col: Int) {
        val rows = _terminalRows[sessionId] ?: 24
        val cols = _terminalCols[sessionId] ?: 120
        _cursorRow[sessionId] = minOf(rows - 1, maxOf(0, row))
        _cursorCol[sessionId] = minOf(cols - 1, maxOf(0, col))
        ensureLineExists(sessionId, _cursorRow[sessionId] ?: 0)
    }

    private fun setCursorRow(sessionId: Int, row: Int) {
        val rows = _terminalRows[sessionId] ?: 24
        _cursorRow[sessionId] = minOf(rows - 1, maxOf(0, row))
        ensureLineExists(sessionId, _cursorRow[sessionId] ?: 0)
    }

    private fun setCursorCol(sessionId: Int, col: Int) {
        val cols = _terminalCols[sessionId] ?: 120
        _cursorCol[sessionId] = minOf(cols - 1, maxOf(0, col))
    }

    private fun eraseDisplay(sessionId: Int, mode: Int) {
        val lines = sessionLinesMap[sessionId] ?: return
        val row = _cursorRow[sessionId] ?: 0
        val col = _cursorCol[sessionId] ?: 0

        when (mode) {
            0 -> {
                // Erase from cursor to end
                if (row < lines.size) {
                    val line = lines[row]
                    for (i in col until line.chars.size) {
                        line.chars[i] = TerminalChar(' ')
                    }
                }
                for (i in row + 1 until lines.size) {
                    lines[i].chars.clear()
                }
            }
            1 -> {
                // Erase from start to cursor
                for (i in 0 until row) {
                    lines[i].chars.clear()
                }
                if (row < lines.size) {
                    val line = lines[row]
                    for (i in 0..minOf(col, line.chars.size - 1)) {
                        line.chars[i] = TerminalChar(' ')
                    }
                }
            }
            2, 3 -> {
                // Erase entire display
                lines.forEach { it.chars.clear() }
                _cursorRow[sessionId] = 0
                _cursorCol[sessionId] = 0
            }
        }
    }

    private fun eraseLine(sessionId: Int, mode: Int) {
        val lines = sessionLinesMap[sessionId] ?: return
        val row = _cursorRow[sessionId] ?: 0
        val col = _cursorCol[sessionId] ?: 0

        if (row >= lines.size) return
        val line = lines[row]

        when (mode) {
            0 -> {
                // Erase from cursor to end of line
                for (i in col until line.chars.size) {
                    line.chars[i] = TerminalChar(' ')
                }
            }
            1 -> {
                // Erase from start of line to cursor
                for (i in 0..minOf(col, line.chars.size - 1)) {
                    line.chars[i] = TerminalChar(' ')
                }
            }
            2 -> {
                // Erase entire line
                line.chars.clear()
            }
        }
    }

    private fun insertLines(sessionId: Int, n: Int) {
        val lines = sessionLinesMap[sessionId] ?: return
        val row = _cursorRow[sessionId] ?: 0
        repeat(n) {
            if (row < lines.size) {
                lines.add(row, TerminalLine())
            }
        }
    }

    private fun deleteLines(sessionId: Int, n: Int) {
        val lines = sessionLinesMap[sessionId] ?: return
        val row = _cursorRow[sessionId] ?: 0
        repeat(n) {
            if (row < lines.size) {
                lines.removeAt(row)
            }
        }
    }

    private fun deleteChars(sessionId: Int, n: Int) {
        val lines = sessionLinesMap[sessionId] ?: return
        val row = _cursorRow[sessionId] ?: 0
        val col = _cursorCol[sessionId] ?: 0

        if (row >= lines.size) return
        val line = lines[row]

        repeat(n) {
            if (col < line.chars.size) {
                line.chars.removeAt(col)
            }
        }
    }

    private fun eraseChars(sessionId: Int, n: Int) {
        val lines = sessionLinesMap[sessionId] ?: return
        val row = _cursorRow[sessionId] ?: 0
        val col = _cursorCol[sessionId] ?: 0

        if (row >= lines.size) return
        val line = lines[row]

        for (i in col until minOf(col + n, line.chars.size)) {
            line.chars[i] = TerminalChar(' ')
        }
    }

    private fun scrollUp(sessionId: Int, n: Int = 1) {
        val lines = sessionLinesMap[sessionId] ?: return
        repeat(n) {
            if (lines.isNotEmpty()) {
                lines.removeAt(0)
            }
            lines.add(TerminalLine())
        }
        // Adjust cursor if needed
        val row = _cursorRow[sessionId] ?: 0
        _cursorRow[sessionId] = maxOf(0, row - n)
    }

    private fun scrollDown(sessionId: Int, n: Int = 1) {
        val lines = sessionLinesMap[sessionId] ?: return
        val rows = _terminalRows[sessionId] ?: 24
        repeat(n) {
            lines.add(0, TerminalLine())
            if (lines.size > rows) {
                lines.removeAt(lines.size - 1)
            }
        }
    }

    private fun ensureLineExists(sessionId: Int, row: Int) {
        val lines = sessionLinesMap[sessionId] ?: return
        while (lines.size <= row) {
            lines.add(TerminalLine())
        }
    }

    fun switchSession(sessionId: Int) {
        if (sessionId in _sessions) {
            currentSessionId = sessionId
        }
    }

    fun closeSession(sessionId: Int) {
        sessionJobs[sessionId]?.cancel()
        try {
            sessionProcesses[sessionId]?.destroyForcibly()
        } catch (_: Exception) {}
        sessionProcesses.remove(sessionId)
        sessionJobs.remove(sessionId)
        sessionLinesMap.remove(sessionId)
        _cursorRow.remove(sessionId)
        _cursorCol.remove(sessionId)
        _terminalCols.remove(sessionId)
        _terminalRows.remove(sessionId)
        _currentFg.remove(sessionId)
        _currentBg.remove(sessionId)
        _currentBold.remove(sessionId)
        _sessions.remove(sessionId)

        if (currentSessionId == sessionId) {
            currentSessionId = _sessions.keys.firstOrNull() ?: -1
        }
    }

    fun clearSession(sessionId: Int) {
        val lines = sessionLinesMap[sessionId] ?: return
        lines.clear()
        lines.add(TerminalLine())
        _cursorRow[sessionId] = 0
        _cursorCol[sessionId] = 0
    }

    fun isSessionRunning(sessionId: Int): Boolean {
        return sessionProcesses[sessionId]?.isAlive == true
    }

    fun getCursorPosition(sessionId: Int): Pair<Int, Int> {
        return Pair(_cursorRow[sessionId] ?: 0, _cursorCol[sessionId] ?: 0)
    }

    fun runCommandInNewSession(
        command: String,
        name: String = "Run",
        environmentVariables: Map<String, String> = emptyMap(),
        customWorkingDir: File? = null
    ): Int {
        val id = nextId++
        val dir = customWorkingDir ?: workingDirectory ?: File(System.getProperty("user.home"))
        val session = TerminalSession(id, name, dir)
        _sessions[id] = session
        sessionLinesMap[id] = mutableStateListOf()

        _cursorRow[id] = 0
        _cursorCol[id] = 0
        _terminalCols[id] = 120
        _terminalRows[id] = 24
        _currentFg[id] = Color.White
        _currentBg[id] = Color.Transparent
        _currentBold[id] = false

        currentSessionId = id

        startPtyProcess(id, dir, environmentVariables)

        viewModelScope.launch {
            kotlinx.coroutines.delay(200)
            sendInput(id, command + "\n")
        }

        return id
    }

    override fun onCleared() {
        super.onCleared()
        _sessions.keys.toList().forEach { closeSession(it) }
    }
}

enum class TerminalKey {
    ENTER, TAB, BACKSPACE, ESCAPE,
    UP, DOWN, LEFT, RIGHT,
    HOME, END, PAGE_UP, PAGE_DOWN,
    INSERT, DELETE,
    F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12
}
