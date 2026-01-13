package com.wannaverse.wannacode.ide.editor.virtualized

data class CursorPosition(
    val line: Int,
    val column: Int
) : Comparable<CursorPosition> {

    override fun compareTo(other: CursorPosition): Int = when {
        line != other.line -> line.compareTo(other.line)
        else -> column.compareTo(other.column)
    }

    fun coerceIn(document: DocumentModel): CursorPosition {
        val maxLine = (document.lineCount - 1).coerceAtLeast(0)
        val clampedLine = line.coerceIn(0, maxLine)
        val maxColumn = document.getLineLength(clampedLine)
        val clampedColumn = column.coerceIn(0, maxColumn)
        return CursorPosition(clampedLine, clampedColumn)
    }

    companion object {
        val ZERO = CursorPosition(0, 0)
    }
}
