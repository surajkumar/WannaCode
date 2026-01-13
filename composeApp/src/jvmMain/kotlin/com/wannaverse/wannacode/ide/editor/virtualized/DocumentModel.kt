package com.wannaverse.wannacode.ide.editor.virtualized

import androidx.compose.runtime.mutableStateListOf

class DocumentModel(initialText: String = "") {
    private val _lines = mutableStateListOf<String>()
    val lines: List<String> get() = _lines

    val lineCount: Int get() = _lines.size

    init {
        setText(initialText)
    }

    fun getLine(index: Int): String = _lines.getOrElse(index) { "" }

    fun getLineLength(index: Int): Int = getLine(index).length

    fun getText(): String = _lines.joinToString("\n")

    fun setText(text: String) {
        _lines.clear()
        if (text.isEmpty()) {
            _lines.add("")
        } else {
            _lines.addAll(text.split("\n"))
        }
    }

    fun insertAt(position: CursorPosition, text: String): CursorPosition {
        if (text.isEmpty()) return position

        val safePos = position.coerceIn(this)
        val currentLine = getLine(safePos.line)
        val insertLines = text.split("\n")

        return if (insertLines.size == 1) {
            // Single line insertion
            val newLine = currentLine.substring(0, safePos.column) +
                text +
                currentLine.substring(safePos.column)
            replaceLine(safePos.line, newLine)
            CursorPosition(safePos.line, safePos.column + text.length)
        } else {
            // Multi-line insertion
            val before = currentLine.substring(0, safePos.column)
            val after = currentLine.substring(safePos.column)

            // Modify first line
            replaceLine(safePos.line, before + insertLines.first())

            // Insert middle lines
            for (i in 1 until insertLines.size - 1) {
                insertLine(safePos.line + i, insertLines[i])
            }

            // Insert last line with remainder
            val lastLineIndex = safePos.line + insertLines.size - 1
            insertLine(lastLineIndex, insertLines.last() + after)

            CursorPosition(lastLineIndex, insertLines.last().length)
        }
    }

    fun deleteRange(start: CursorPosition, end: CursorPosition): CursorPosition {
        if (start == end) return start

        val (actualStart, actualEnd) = if (start < end) start to end else end to start
        val safeStart = actualStart.coerceIn(this)
        val safeEnd = actualEnd.coerceIn(this)

        if (safeStart == safeEnd) return safeStart

        return if (safeStart.line == safeEnd.line) {
            // Single line deletion
            val line = getLine(safeStart.line)
            val newLine = line.substring(0, safeStart.column) +
                line.substring(safeEnd.column.coerceAtMost(line.length))
            replaceLine(safeStart.line, newLine)
            safeStart
        } else {
            // Multi-line deletion
            val startLine = getLine(safeStart.line)
            val endLine = getLine(safeEnd.line)

            val newLine = startLine.substring(0, safeStart.column) +
                endLine.substring(safeEnd.column.coerceAtMost(endLine.length))

            // Remove lines from end to start+1 (reverse order)
            for (i in safeEnd.line downTo safeStart.line + 1) {
                removeLine(i)
            }

            // Replace start line with merged content
            replaceLine(safeStart.line, newLine)
            safeStart
        }
    }

    fun getTextInRange(start: CursorPosition, end: CursorPosition): String {
        if (start == end) return ""

        val (actualStart, actualEnd) = if (start < end) start to end else end to start
        val safeStart = actualStart.coerceIn(this)
        val safeEnd = actualEnd.coerceIn(this)

        if (safeStart == safeEnd) return ""

        return if (safeStart.line == safeEnd.line) {
            val line = getLine(safeStart.line)
            line.substring(safeStart.column, safeEnd.column.coerceAtMost(line.length))
        } else {
            val result = StringBuilder()
            // First line
            val firstLine = getLine(safeStart.line)
            result.append(firstLine.substring(safeStart.column))

            // Middle lines
            for (i in safeStart.line + 1 until safeEnd.line) {
                result.append("\n")
                result.append(getLine(i))
            }

            // Last line
            result.append("\n")
            val lastLine = getLine(safeEnd.line)
            result.append(lastLine.substring(0, safeEnd.column.coerceAtMost(lastLine.length)))

            result.toString()
        }
    }

    fun replaceLine(index: Int, newContent: String) {
        if (index in _lines.indices) {
            _lines[index] = newContent
        }
    }

    fun insertLine(index: Int, content: String) {
        val safeIndex = index.coerceIn(0, _lines.size)
        _lines.add(safeIndex, content)
    }

    fun removeLine(index: Int) {
        if (index in _lines.indices && _lines.size > 1) {
            _lines.removeAt(index)
        } else if (_lines.size == 1) {
            _lines[0] = ""
        }
    }

    fun joinLines(lineIndex: Int): CursorPosition {
        if (lineIndex < 0 || lineIndex >= lineCount - 1) {
            return CursorPosition(lineIndex.coerceIn(0, (lineCount - 1).coerceAtLeast(0)), 0)
        }

        val currentLine = getLine(lineIndex)
        val nextLine = getLine(lineIndex + 1)
        val joinColumn = currentLine.length

        replaceLine(lineIndex, currentLine + nextLine)
        removeLine(lineIndex + 1)

        return CursorPosition(lineIndex, joinColumn)
    }
}
