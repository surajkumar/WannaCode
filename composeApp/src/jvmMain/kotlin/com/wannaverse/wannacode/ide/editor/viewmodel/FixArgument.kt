package com.wannaverse.wannacode.ide.editor.viewmodel

import kotlinx.serialization.Serializable

@Serializable
data class FixArgument(
    val changes: Map<String, List<Change>>
)
