package com.wannaverse.wannacode.ide.editor.virtualized

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint

object KeyboardHandler {

    private val nonCharacterKeys = setOf(
        Key.ShiftLeft, Key.ShiftRight,
        Key.CtrlLeft, Key.CtrlRight,
        Key.AltLeft, Key.AltRight,
        Key.MetaLeft, Key.MetaRight,
        Key.CapsLock, Key.NumLock, Key.ScrollLock,
        Key.Escape, Key.PrintScreen,
        Key.Insert,
        Key.F1, Key.F2, Key.F3, Key.F4, Key.F5, Key.F6,
        Key.F7, Key.F8, Key.F9, Key.F10, Key.F11, Key.F12,
        Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight,
        Key.Home, Key.MoveEnd, Key.PageUp, Key.PageDown,
        Key.Backspace, Key.Delete, Key.Enter, Key.Tab,
        Key.Unknown
    )

    fun handleKeyEvent(
        event: KeyEvent,
        state: VirtualizedEditorState,
        onTextChange: () -> Unit
    ): Boolean {
        if (event.type != KeyEventType.KeyDown) return false

        val isShift = event.isShiftPressed
        val isCtrl = event.isCtrlPressed
        val isMeta = event.isMetaPressed
        val isAlt = event.isAltPressed
        val isModifier = isCtrl || isMeta

        return when {
            event.key == Key.DirectionLeft -> {
                handleLeftArrow(state, isShift, isModifier)
                true
            }
            event.key == Key.DirectionRight -> {
                handleRightArrow(state, isShift, isModifier)
                true
            }
            event.key == Key.DirectionUp -> {
                handleUpArrow(state, isShift)
                true
            }
            event.key == Key.DirectionDown -> {
                handleDownArrow(state, isShift)
                true
            }
            event.key == Key.Home -> {
                handleHome(state, isShift, isModifier)
                true
            }
            event.key == Key.MoveEnd -> {
                handleEnd(state, isShift, isModifier)
                true
            }
            event.key == Key.PageUp -> {
                handlePageUp(state, isShift)
                true
            }
            event.key == Key.PageDown -> {
                handlePageDown(state, isShift)
                true
            }

            event.key == Key.Backspace && !state.readOnly -> {
                state.delete(DeleteDirection.BACKWARD)
                onTextChange()
                true
            }
            event.key == Key.Delete && !state.readOnly -> {
                state.delete(DeleteDirection.FORWARD)
                onTextChange()
                true
            }
            event.key == Key.Enter && !state.readOnly -> {
                state.insert("\n")
                onTextChange()
                true
            }
            event.key == Key.Tab && !state.readOnly -> {
                state.insert("    ")
                onTextChange()
                true
            }

            isModifier && event.key == Key.A -> {
                state.selectAll()
                true
            }
            isModifier && event.key == Key.C -> {
                state.getSelectedText()?.let { ClipboardManager.copy(it) }
                true
            }
            isModifier && event.key == Key.V && !state.readOnly -> {
                ClipboardManager.paste()?.let { state.insert(it) }
                onTextChange()
                true
            }
            isModifier && event.key == Key.X && !state.readOnly -> {
                ClipboardManager.cut(state)
                onTextChange()
                true
            }

            event.key in nonCharacterKeys -> false

            isModifier || isAlt -> false

            !state.readOnly && event.utf16CodePoint > 31 -> {
                val char = Char(event.utf16CodePoint)
                if (char.isDefined() && !char.isISOControl()) {
                    state.insert(char.toString())
                    onTextChange()
                    true
                } else false
            }

            else -> false
        }
    }

    private fun handleLeftArrow(state: VirtualizedEditorState, shift: Boolean, modifier: Boolean) {
        val newPos = if (modifier) {
            findPreviousWord(state.document, state.cursor)
        } else {
            moveLeft(state.document, state.cursor)
        }
        state.moveCursor(newPos, extendSelection = shift)
    }

    private fun handleRightArrow(state: VirtualizedEditorState, shift: Boolean, modifier: Boolean) {
        val newPos = if (modifier) {
            findNextWord(state.document, state.cursor)
        } else {
            moveRight(state.document, state.cursor)
        }
        state.moveCursor(newPos, extendSelection = shift)
    }

    private fun handleUpArrow(state: VirtualizedEditorState, shift: Boolean) {
        val newPos = moveUp(state.document, state.cursor)
        state.moveCursor(newPos, extendSelection = shift)
    }

    private fun handleDownArrow(state: VirtualizedEditorState, shift: Boolean) {
        val newPos = moveDown(state.document, state.cursor)
        state.moveCursor(newPos, extendSelection = shift)
    }

    private fun handleHome(state: VirtualizedEditorState, shift: Boolean, modifier: Boolean) {
        val newPos = if (modifier) {
            CursorPosition.ZERO
        } else {
            CursorPosition(state.cursor.line, 0)
        }
        state.moveCursor(newPos, extendSelection = shift)
    }

    private fun handleEnd(state: VirtualizedEditorState, shift: Boolean, modifier: Boolean) {
        val newPos = if (modifier) {
            val lastLine = (state.document.lineCount - 1).coerceAtLeast(0)
            CursorPosition(lastLine, state.document.getLineLength(lastLine))
        } else {
            CursorPosition(state.cursor.line, state.document.getLineLength(state.cursor.line))
        }
        state.moveCursor(newPos, extendSelection = shift)
    }

    private fun handlePageUp(state: VirtualizedEditorState, shift: Boolean) {
        val newLine = (state.cursor.line - state.visibleLineCount).coerceAtLeast(0)
        val newCol = state.cursor.column.coerceAtMost(state.document.getLineLength(newLine))
        state.moveCursor(CursorPosition(newLine, newCol), extendSelection = shift)
    }

    private fun handlePageDown(state: VirtualizedEditorState, shift: Boolean) {
        val newLine = (state.cursor.line + state.visibleLineCount).coerceAtMost(
            (state.document.lineCount - 1).coerceAtLeast(0)
        )
        val newCol = state.cursor.column.coerceAtMost(state.document.getLineLength(newLine))
        state.moveCursor(CursorPosition(newLine, newCol), extendSelection = shift)
    }

    private fun moveLeft(document: DocumentModel, cursor: CursorPosition): CursorPosition = if (cursor.column > 0) {
        CursorPosition(cursor.line, cursor.column - 1)
    } else if (cursor.line > 0) {
        CursorPosition(cursor.line - 1, document.getLineLength(cursor.line - 1))
    } else {
        cursor
    }

    private fun moveRight(document: DocumentModel, cursor: CursorPosition): CursorPosition {
        val lineLength = document.getLineLength(cursor.line)
        return if (cursor.column < lineLength) {
            CursorPosition(cursor.line, cursor.column + 1)
        } else if (cursor.line < document.lineCount - 1) {
            CursorPosition(cursor.line + 1, 0)
        } else {
            cursor
        }
    }

    private fun moveUp(document: DocumentModel, cursor: CursorPosition): CursorPosition = if (cursor.line > 0) {
        val newLine = cursor.line - 1
        val newCol = cursor.column.coerceAtMost(document.getLineLength(newLine))
        CursorPosition(newLine, newCol)
    } else {
        CursorPosition(cursor.line, 0)
    }

    private fun moveDown(document: DocumentModel, cursor: CursorPosition): CursorPosition = if (cursor.line < document.lineCount - 1) {
        val newLine = cursor.line + 1
        val newCol = cursor.column.coerceAtMost(document.getLineLength(newLine))
        CursorPosition(newLine, newCol)
    } else {
        CursorPosition(cursor.line, document.getLineLength(cursor.line))
    }

    private fun findPreviousWord(document: DocumentModel, cursor: CursorPosition): CursorPosition {
        val line = document.getLine(cursor.line)
        var col = cursor.column

        while (col > 0 && line.getOrNull(col - 1)?.isWhitespace() == true) {
            col--
        }

        while (col > 0 && line.getOrNull(col - 1)?.let { it.isLetterOrDigit() || it == '_' } == true) {
            col--
        }

        return if (col == cursor.column && cursor.line > 0) {
            CursorPosition(cursor.line - 1, document.getLineLength(cursor.line - 1))
        } else {
            CursorPosition(cursor.line, col)
        }
    }

    private fun findNextWord(document: DocumentModel, cursor: CursorPosition): CursorPosition {
        val line = document.getLine(cursor.line)
        var col = cursor.column

        while (col < line.length && line[col].let { it.isLetterOrDigit() || it == '_' }) {
            col++
        }

        while (col < line.length && line[col].isWhitespace()) {
            col++
        }

        return if (col == cursor.column && cursor.line < document.lineCount - 1) {
            CursorPosition(cursor.line + 1, 0)
        } else {
            CursorPosition(cursor.line, col)
        }
    }
}
