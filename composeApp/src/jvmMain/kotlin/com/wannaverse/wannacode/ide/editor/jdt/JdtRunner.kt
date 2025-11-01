package com.wannaverse.wannacode.ide.editor.jdt

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import java.io.File
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionCapabilities
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionLiteralSupportCapabilities
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.PublishDiagnosticsCapabilities
import org.eclipse.lsp4j.TextDocumentClientCapabilities
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageServer

var launcher: Launcher<LanguageServer>? = null
var process: Process? = null

var workspaceFile: File? = null

fun launchJdtServer(workspace: File?) {
    workspaceFile = workspace

    val eclipseJdrDirectory: File = File("../eclipse-jdt").absoluteFile
    val configDir = eclipseJdrDirectory.resolve("config_win")
    val launcherScript = eclipseJdrDirectory.resolve("bin/jdtls.bat")

    process = ProcessBuilder(
        launcherScript.absolutePath,
        "-configuration",
        configDir.absolutePath,
        "-data",
        workspace?.absolutePath
    )
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    val client = JavaClient()
    launcher = LSPLauncher.createClientLauncher(client, process?.inputStream, process?.outputStream)
    val server = launcher!!.remoteProxy
    launcher!!.startListening()

    val initParams = InitializeParams().apply {
        processId = ProcessHandle.current().pid().toInt()
        capabilities = ClientCapabilities().apply {
            textDocument = TextDocumentClientCapabilities().apply {
                codeAction = CodeActionCapabilities().apply { codeActionLiteralSupport = CodeActionLiteralSupportCapabilities() }
                publishDiagnostics = PublishDiagnosticsCapabilities(true)
            }
        }
    }

    try {
        val initResult = server.initialize(initParams).get()
        println("Initialized: ${initResult.capabilities.textDocumentSync?.get()}")
    } catch (e: Exception) {
        println("Initialize failed: ${e.message}")
        process?.errorStream?.bufferedReader()?.forEachLine { println("ERROR: $it") }
        return
    }
}

fun restartJdtServer() {
    process?.destroy()
    process?.onExit()?.thenAccept { _ ->
        launchJdtServer(workspaceFile)
    }
}

fun getDiagnostics(code: String, fileLocation: String) {
    val file = "file://" + fileLocation.replace("\\", "/")
    launcher?.remoteProxy?.textDocumentService?.didOpen(
        DidOpenTextDocumentParams(
            TextDocumentItem(
                file,
                "java",
                1,
                code
            )
        )
    )
}

data class Wrapper(val diagnostics: List<Diagnostic>, val fixes: MutableList<Either<Command, CodeAction>>)

val quickFixes: SnapshotStateMap<String, Wrapper> = mutableStateMapOf()

fun getQuickFixForDiagnostics(fileLocation: String, diagnostics: List<Diagnostic>?) {
    val server = launcher?.remoteProxy

    diagnostics?.forEach { diagnostic ->
        val context = CodeActionContext(listOf(diagnostic))

        server?.textDocumentService?.codeAction(
            CodeActionParams(
                TextDocumentIdentifier(fileLocation),
                diagnostic.range,
                context
            )
        )?.thenAccept { actions ->
            if (!actions.isNullOrEmpty()) {
//                actions.forEach { action ->
//                    println(action.left.title)
//                }
                quickFixes[fileLocation] = Wrapper(
                    diagnostics = diagnostics,
                    fixes = actions
                )
            }
        }?.exceptionally { _ ->
            null
        }
    }
}

fun applyQuickFix(command: Command) {
    launcher?.remoteProxy?.workspaceService?.executeCommand(ExecuteCommandParams(command.command, command.arguments))
}
