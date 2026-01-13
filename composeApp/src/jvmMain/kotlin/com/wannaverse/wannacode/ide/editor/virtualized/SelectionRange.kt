package com.wannaverse.wannacode.ide.editor.virtualized

data class SelectionRange(
    val anchor: CursorPosition,
    val focus: CursorPosition
) {
    val start: CursorPosition get() = minOf(anchor, focus)
    val end: CursorPosition get() = maxOf(anchor, focus)

    val isCollapsed: Boolean get() = anchor == focus
    val isReversed: Boolean get() = focus < anchor

    fun contains(position: CursorPosition): Boolean = position >= start && position < end

    fun spansLine(lineIndex: Int): Boolean = lineIndex >= start.line && lineIndex <= end.line

    fun getBoundsForLine(lineIndex: Int, lineLength: Int): Pair<Int, Int>? {
        if (!spansLine(lineIndex)) return null

        val startCol = if (lineIndex == start.line) start.column else 0
        val endCol = if (lineIndex == end.line) end.column else lineLength

        return startCol to endCol
    }
}
