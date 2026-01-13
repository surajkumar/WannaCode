package com.wannaverse.wannacode.ide.editor.virtualized

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

object ClipboardManager {
    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    fun copy(text: String) {
        clipboard.setContents(StringSelection(text), null)
    }

    fun paste(): String? = try {
        clipboard.getData(DataFlavor.stringFlavor) as? String
    } catch (_: Exception) {
        null
    }

    fun cut(state: VirtualizedEditorState): String? {
        val text = state.getSelectedText() ?: return null
        copy(text)
        state.deleteSelection()
        return text
    }
}
