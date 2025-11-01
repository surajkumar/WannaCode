package com.wannaverse.wannacode.ide.editor.jdt

import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.services.LanguageClient

class JavaClient : LanguageClient {

    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams?) {
        val fileUri = diagnostics?.uri ?: return
        if (!diagnostics.diagnostics.isNullOrEmpty()) {
            println("Received diagnostics for $fileUri ")
            getQuickFixForDiagnostics(fileUri, diagnostics.diagnostics)
        }
    }

    override fun telemetryEvent(`object`: Any?) {}

    override fun showMessage(messageParams: MessageParams?) {}

    override fun showMessageRequest(requestParams: ShowMessageRequestParams?): CompletableFuture<MessageActionItem?>? = null

    override fun logMessage(message: MessageParams?) {
        if (message?.type == MessageType.Error) {
            if (message.message?.contains("Document does not match the AST") == true) {
                println("JDT server restarting")
                restartJdtServer()
            }
        }
    }
}
