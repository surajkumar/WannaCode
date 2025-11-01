package com.wannaverse.wannacode.ide.editor.viewmodel

import kotlinx.serialization.Serializable

@Serializable
data class Range(
    val start: Position,
    val end: Position
)
