package com.wannaverse.wannacode.ide.editor.syntax

import androidx.compose.ui.text.SpanStyle

data class SyntaxRule(
    val pattern: Regex,
    val style: SpanStyle
)
