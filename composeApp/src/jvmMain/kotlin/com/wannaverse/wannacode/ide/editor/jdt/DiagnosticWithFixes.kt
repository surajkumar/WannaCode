package com.wannaverse.wannacode.ide.editor.jdt

import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.jsonrpc.messages.Either

data class DiagnosticWithFixes(
    val diagnostic: Diagnostic,
    val quickFixes: List<Either<CodeAction, Command>>
)
