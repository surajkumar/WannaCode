package com.wannaverse.wannacode.ide.editor

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.wannaverse.wannacode.ide.editor.jdt.applyQuickFix
import com.wannaverse.wannacode.ide.editor.jdt.getDiagnostics
import com.wannaverse.wannacode.ide.editor.jdt.launchJdtServer
import com.wannaverse.wannacode.ide.editor.jdt.quickFixes
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import kotlin.collections.mutableListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.WorkspaceEdit

data class TabContent(
    val id: Int,
    val file: File,
    val text: String,
    val isDirty: Boolean = false,
    val readOnly: Boolean = false
)

class CodeEditorViewModel : ViewModel() {
    private var nextId = 0
        get() = field++

    private val activeTabTitles = mutableStateListOf<String>()
    private val _tabContents = mutableStateMapOf<Int, TabContent>()
    val tabContents: Map<Int, TabContent> = _tabContents
    val currentTab = mutableStateOf(-1)

    val projectName = mutableStateOf("")
    val directory = mutableStateOf(File(""))

    private val tabDiagnostics = mutableStateMapOf<Int, MutableState<List<DiagnosticLineInfo>>>()
    val diagnosticLineInfoList: State<List<DiagnosticLineInfo>>
        get() = tabDiagnostics.getOrPut(currentTab.value) { mutableStateOf(emptyList()) }

    // Per-tab logs
    private val tabLogs = mutableStateMapOf<Int, MutableState<List<String>>>()

    fun addLog(msg: String, tabId: Int = currentTab.value) {
        val state = tabLogs.getOrPut(tabId) { mutableStateOf(emptyList()) }
        state.value += msg // trigger recomposition
    }

    fun clearLogs(tabId: Int = currentTab.value) {
        tabLogs[tabId]?.value = emptyList()
    }

    fun getLogs(tabId: Int = currentTab.value): State<List<String>> = tabLogs.getOrPut(tabId) { mutableStateOf(emptyList()) }
    var refreshKey by mutableStateOf(0)
        private set

    fun refreshTree() {
        refreshKey++
    }

    fun startJdtServer(project: File) {
        launchJdtServer(workspace = File(project.parent))
    }

    fun getDiagnostics(file: File, content: String) {
        if (!file.name.endsWith(".java")) {
            return
        }
        return getDiagnostics(
            code = content,
            fileLocation = file.absolutePath
        )
    }

    data class DiagnosticLineInfo(
        val diagnosticLine: Int,
        val startChar: Int, // column where the error starts
        val endChar: Int, // column where the error ends
        val message: String,
        val fixes: List<FixArgument>
    )

    @Serializable
    data class FixArgument(
        val changes: Map<String, List<Change>>
    )

    @Serializable
    data class Change(
        val range: Range,
        val newText: String,
        var title: String? = null,
        var command: Command? = null
    )

    @Serializable
    data class Range(
        val start: Position,
        val end: Position
    )

    @Serializable
    data class Position(
        val line: Int,
        val character: Int
    )

    val gson = Gson()

    fun applyFix(change: Change) {
        val tabId = currentTab.value
        val tab = _tabContents[tabId] ?: return
        var newText = tab.text

        // Apply direct text edits if present
        change.range?.let { range ->
            val lines = newText.lines().toMutableList()
            val startLine = range.start.line
            val startChar = range.start.character
            val endLine = range.end.line
            val endChar = range.end.character

            val before = lines[startLine].substring(0, startChar)
            val after = lines[endLine].substring(endChar)

            val newLines = change.newText.split("\n")

            lines.subList(startLine, endLine + 1).clear()
            lines.addAll(startLine, newLines)
            lines[startLine] = before + lines[startLine]
            lines[startLine + newLines.size - 1] += after

            newText = lines.joinToString("\n")
        }

        // Update the editor tab
        updateCurrentTabText(newText)

        // If a Command is attached, apply it (e.g., java.apply.workspaceEdit)
        change.command?.let { command ->
            if (command.command == "java.apply.workspaceEdit") {
                // Parse the workspace edit from command arguments
                val gson = Gson()
                val editJson = gson.toJson(command.arguments?.get(0))
                val workspaceEdit = gson.fromJson(editJson, WorkspaceEdit::class.java)
                applyWorkspaceEdit(workspaceEdit)
            } else {
                applyQuickFix(command)
            }
        }
    }

    // Utility to apply a WorkspaceEdit
    fun applyWorkspaceEdit(edit: WorkspaceEdit) {
        edit.changes?.forEach { (uri, textEdits) ->
            val filePath = uri.removePrefix("file://").replace("/", File.separator)
            val file = File(filePath)
            if (!file.exists()) return@forEach

            var code = file.readText()
            textEdits.sortedByDescending { it.range.start.line * 1000 + it.range.start.character }.forEach { te ->
                val lines = code.lines().toMutableList()
                val startLine = te.range.start.line
                val startChar = te.range.start.character
                val endLine = te.range.end.line
                val endChar = te.range.end.character

                val before = lines[startLine].substring(0, startChar)
                val after = lines[endLine].substring(endChar)

                val newLines = te.newText.split("\n")
                lines.subList(startLine, endLine + 1).clear()
                lines.addAll(startLine, newLines)
                lines[startLine] = before + lines[startLine]
                lines[startLine + newLines.size - 1] += after

                code = lines.joinToString("\n")
            }
            file.writeText(code)
        }
    }

    fun populateQuickFixesByLine(tabId: Int, file: String) {
        val newList = mutableListOf<DiagnosticLineInfo>()

        val diagnostics = quickFixes[file]?.diagnostics ?: return
        val fixes = quickFixes[file]?.fixes ?: return

        val fixArgumentsList = mutableListOf<FixArgument>()

        fixes.forEach { fix ->
            val command = fix.left
            val arguments = command.arguments
            val title = command.title

            arguments.forEach { arg ->
                if (arg is JsonObject) {
                    val jsonString = gson.toJson(arg)
                    val fixArg = gson.fromJson(jsonString, FixArgument::class.java)

                    fixArg.changes.values.flatten().forEach { change ->
                        change.title = title
                        change.command = command
                    }

                    fixArgumentsList.add(fixArg)
                }
            }
        }

        diagnostics.forEach { diag ->
            val msg = "Line ${diag.range.start.line}: ${diag.message}"
            addLog(msg, tabId)

            newList.add(
                DiagnosticLineInfo(
                    diagnosticLine = diag.range.start.line,
                    startChar = diag.range.start.character,
                    endChar = diag.range.end.character,
                    message = diag.message,
                    fixes = fixArgumentsList
                )
            )
        }

        tabDiagnostics[tabId] = mutableStateOf(newList)
    }

    val orderedTabIds: List<Int>
        get() = activeTabTitles.mapNotNull { title ->
            _tabContents.entries.find { it.value.file.name == title }?.key
        }

    fun showTab(tabId: Int) {
        if (tabId in _tabContents) {
            currentTab.value = tabId
        }
    }

    fun closeTab(tabId: Int) {
        val tab = _tabContents[tabId] ?: return

        activeTabTitles.remove(tab.file.name)
        _tabContents.remove(tabId)
        tabLogs.remove(tabId)
        tabDiagnostics.remove(tabId)

        if (currentTab.value == tabId) {
            currentTab.value = activeTabTitles.firstOrNull()?.let { title ->
                _tabContents.entries.find { it.value.file.name == title }?.key
            } ?: -1
        }
    }

    fun openFile(file: File) {
        if (!file.isFile) return

        val existingTab = _tabContents.values.find { it.file == file }
        if (existingTab != null) {
            showTab(existingTab.id)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = file.toPath().readTextSafe()

                withContext(Dispatchers.Main) {
                    val tabId = nextId
                    val title = file.name

                    activeTabTitles.add(title)
                    _tabContents[tabId] = TabContent(
                        id = tabId,
                        file = file,
                        text = content,
                        readOnly = file.toPath().isLikelyBinary()
                    )

                    // Initialize per-tab logs and diagnostics
                    tabLogs[tabId] = mutableStateOf(emptyList())
                    tabDiagnostics[tabId] = mutableStateOf(emptyList())

                    showTab(tabId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun Path.readTextSafe(
        charset: Charset = Charsets.UTF_8,
        replacement: Char = '�'
    ): String {
        // Fast check: if file has null bytes or too many control chars → binary
        if (isLikelyBinary()) {
            return "<binary file - cannot display as text>"
        }

        val bytes = Files.readAllBytes(this)
        val decoder = charset.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
            .replaceWith(replacement.toString())

        return decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
    }

    fun Path.isLikelyBinary(previewSize: Long = 1024): Boolean {
        if (!Files.isRegularFile(this)) return true
        if (Files.size(this) == 0L) return false

        val sizeToCheck = minOf(Files.size(this), previewSize)
        val buffer = ByteArray(sizeToCheck.toInt())

        Files.newInputStream(this).use { stream ->
            val read = stream.read(buffer, 0, buffer.size)
            if (read <= 0) return false
            var nullCount = 0
            var controlCount = 0
            for (i in 0 until read) {
                val b = buffer[i].toInt() and 0xFF
                when {
                    b == 0 -> nullCount++
                    b < 32 && b !in setOf(9, 10, 13) -> controlCount++
                }
            }
            val controlRatio = controlCount.toDouble() / read
            return nullCount > 0 || controlRatio > 0.05
        }
    }

    private val tabTexts = mutableStateMapOf<Int, MutableStateFlow<String>>()

    fun updateCurrentTabText(newText: String) {
        val id = currentTab.value
        val tab = _tabContents[id] ?: return
        _tabContents[id] = tab.copy(text = newText, isDirty = true)
        tabTexts[id]?.value = newText
        debounceSave(tab.file, newText)
    }

    private var saveJob: Job? = null

    private fun debounceSave(file: File, text: String) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            saveFile(file, text)
        }
    }

    fun saveFile(file: File, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                file.writeText(content)

                withContext(Dispatchers.Main) {
                    val tab = _tabContents.values.find { it.file == file } ?: return@withContext
                    _tabContents[tab.id] = tab.copy(isDirty = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
