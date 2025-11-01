package com.wannaverse.wannacode.ide.editor.viewmodel

import kotlinx.serialization.Serializable

@Serializable
data class Position(
    val line: Int,
    val character: Int
)
