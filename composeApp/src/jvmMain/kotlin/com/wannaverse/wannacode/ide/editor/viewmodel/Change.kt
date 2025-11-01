package com.wannaverse.wannacode.ide.editor.viewmodel

import kotlinx.serialization.Serializable
import org.eclipse.lsp4j.Command

@Serializable
data class Change(
    val range: Range,
    val newText: String,
    var title: String? = null,
    var command: Command? = null
)
