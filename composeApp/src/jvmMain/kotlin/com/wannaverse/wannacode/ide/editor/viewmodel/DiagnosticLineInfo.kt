package com.wannaverse.wannacode.ide.editor.viewmodel

data class DiagnosticLineInfo(
    val diagnosticLine: Int,
    val startChar: Int,
    val endChar: Int,
    val message: String,
    val fixes: List<FixArgument>
)
