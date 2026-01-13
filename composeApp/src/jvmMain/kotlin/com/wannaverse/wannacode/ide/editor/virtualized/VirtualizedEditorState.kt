package com.wannaverse.wannacode.ide.editor.virtualized

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextMeasurer

class VirtualizedEditorState(
    initialText: String = "",
    val readOnly: Boolean = false
) {
    val document = DocumentModel(initialText)

    var cursor by mutableStateOf(CursorPosition.ZERO)
        private set

    var selection by mutableStateOf<SelectionRange?>(null)
        private set

    val scrollState = LazyListState()

    var textMeasurer: TextMeasurer? = null

    var lineHeightPx by mutableStateOf(24f)

    var charWidthPx by mutableStateOf(8f)

    var isFocused by mutableStateOf(false)

    var visibleLineCount by mutableStateOf(30)

    fun moveCursor(newPosition: CursorPosition, extendSelection: Boolean = false) {
        val clampedPosition = newPosition.coerceIn(document)

        if (extendSelection) {
            val anchor = selection?.anchor ?: cursor
            selection = SelectionRange(anchor, clampedPosition)
        } else {
            selection = null
        }

        cursor = clampedPosition
    }

    fun updateSelection(range: SelectionRange?) {
        selection = range
        range?.let { cursor = it.focus.coerceIn(document) }
    }

    fun clearSelection() {
        selection = null
    }

    fun selectAll() {
        val lastLine = (document.lineCount - 1).coerceAtLeast(0)
        val lastColumn = document.getLineLength(lastLine)
        selection = SelectionRange(
            anchor = CursorPosition.ZERO,
            focus = CursorPosition(lastLine, lastColumn)
        )
        cursor = CursorPosition(lastLine, lastColumn)
    }

    fun selectWord(position: CursorPosition) {
        val pos = position.coerceIn(document)
        val line = document.getLine(pos.line)
        if (line.isEmpty()) return

        val col = pos.column.coerceIn(0, line.length - 1)
        val char = line.getOrNull(col) ?: return

        var start = col
        var end = col

        if (char.isLetterOrDigit() || char == '_') {
            while (start > 0 && (line[start - 1].isLetterOrDigit() || line[start - 1] == '_')) {
                start--
            }
            while (end < line.length && (line[end].isLetterOrDigit() || line[end] == '_')) {
                end++
            }
        } else {
            end = col + 1
        }

        selection = SelectionRange(
            anchor = CursorPosition(pos.line, start),
            focus = CursorPosition(pos.line, end)
        )
        cursor = CursorPosition(pos.line, end)
    }

    fun selectLine(lineIndex: Int) {
        val safeLine = lineIndex.coerceIn(0, (document.lineCount - 1).coerceAtLeast(0))
        val endPos = if (safeLine < document.lineCount - 1) {
            CursorPosition(safeLine + 1, 0)
        } else {
            CursorPosition(safeLine, document.getLineLength(safeLine))
        }

        selection = SelectionRange(
            anchor = CursorPosition(safeLine, 0),
            focus = endPos
        )
        cursor = endPos
    }

    fun insert(text: String): CursorPosition {
        if (readOnly || text.isEmpty()) return cursor

        selection?.let { sel ->
            document.deleteRange(sel.start, sel.end)
            cursor = sel.start
            selection = null
        }

        val newCursor = document.insertAt(cursor, text)
        cursor = newCursor
        return newCursor
    }

    fun delete(direction: DeleteDirection): CursorPosition {
        if (readOnly) return cursor

        selection?.let { sel ->
            val newCursor = document.deleteRange(sel.start, sel.end)
            cursor = newCursor
            selection = null
            return newCursor
        }

        return when (direction) {
            DeleteDirection.BACKWARD -> {
                if (cursor.column > 0) {
                    val deleteStart = CursorPosition(cursor.line, cursor.column - 1)
                    document.deleteRange(deleteStart, cursor)
                    cursor = deleteStart
                    deleteStart
                } else if (cursor.line > 0) {
                    // Join with previous line
                    val newCursor = document.joinLines(cursor.line - 1)
                    cursor = newCursor
                    newCursor
                } else {
                    cursor
                }
            }
            DeleteDirection.FORWARD -> {
                val lineLength = document.getLineLength(cursor.line)
                if (cursor.column < lineLength) {
                    val deleteEnd = CursorPosition(cursor.line, cursor.column + 1)
                    document.deleteRange(cursor, deleteEnd)
                    cursor
                } else if (cursor.line < document.lineCount - 1) {
                    document.joinLines(cursor.line)
                    cursor
                } else {
                    cursor
                }
            }
        }
    }

    fun deleteSelection(): CursorPosition {
        val sel = selection ?: return cursor
        val newCursor = document.deleteRange(sel.start, sel.end)
        cursor = newCursor
        selection = null
        return newCursor
    }

    fun getSelectedText(): String? {
        val sel = selection ?: return null
        if (sel.isCollapsed) return null
        return document.getTextInRange(sel.start, sel.end)
    }

    suspend fun scrollToCursor() {
        val targetLine = cursor.line
        val firstVisible = scrollState.firstVisibleItemIndex
        val lastVisible = firstVisible + visibleLineCount - 1

        when {
            targetLine < firstVisible -> {
                scrollState.animateScrollToItem(targetLine)
            }
            targetLine > lastVisible -> {
                scrollState.animateScrollToItem((targetLine - visibleLineCount + 1).coerceAtLeast(0))
            }
        }
    }
}

enum class DeleteDirection { BACKWARD, FORWARD }
