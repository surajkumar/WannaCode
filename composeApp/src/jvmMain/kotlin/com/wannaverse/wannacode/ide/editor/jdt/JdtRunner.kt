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

private enum class Platform {
    WINDOWS,
    LINUX,
    MAC
}

private fun detectPlatform(): Platform {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains("win") -> Platform.WINDOWS
        osName.contains("mac") || osName.contains("darwin") -> Platform.MAC
        else -> Platform.LINUX
    }
}

private fun getJavaHome(): String {
    System.getenv("JAVA_HOME")?.let { return it }

    val javaHome = System.getProperty("java.home")
    val javaHomeFile = File(javaHome)
    return if (javaHomeFile.name == "jre" && javaHomeFile.parentFile?.resolve("bin/javac")?.exists() == true) {
        javaHomeFile.parentFile.absolutePath
    } else {
        javaHome
    }
}

fun launchJdtServer(workspace: File?) {
    workspaceFile = workspace

    val platform = detectPlatform()
    val eclipseJdtDirectory: File = File("../eclipse-jdt").absoluteFile

    if (!eclipseJdtDirectory.exists()) {
        println("Eclipse JDT directory not found at: ${eclipseJdtDirectory.absolutePath}")
        println("Please download and extract eclipse.jdt.ls to the eclipse-jdt directory")
        return
    }

    val configDirName = when (platform) {
        Platform.WINDOWS -> "config_win"
        Platform.MAC -> "config_mac"
        Platform.LINUX -> "config_linux"
    }

    val scriptName = when (platform) {
        Platform.WINDOWS -> "bin/jdtls.bat"
        else -> "bin/jdtls"
    }

    val configDir = eclipseJdtDirectory.resolve(configDirName)
    val launcherScript = eclipseJdtDirectory.resolve(scriptName)

    if (!launcherScript.exists()) {
        println("JDT launcher script not found at: ${launcherScript.absolutePath}")
        return
    }

    if (platform != Platform.WINDOWS) {
        launcherScript.setExecutable(true)
    }

    val javaHome = getJavaHome()
    println("Using JAVA_HOME: $javaHome")

    val processBuilder = ProcessBuilder(
        launcherScript.absolutePath,
        "-configuration",
        configDir.absolutePath,
        "-data",
        workspace?.absolutePath
    )
        .redirectError(ProcessBuilder.Redirect.PIPE)

    processBuilder.environment()["JAVA_HOME"] = javaHome

    process = processBuilder.start()

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

data class Wrapper(
    val diagnostics: List<Diagnostic>,
    val fixes: MutableList<Either<Command, CodeAction>> = mutableListOf()
)

val quickFixes: SnapshotStateMap<String, Wrapper> = mutableStateMapOf()

var onDiagnosticsUpdated: ((String) -> Unit)? = null

fun getQuickFixForDiagnostics(fileLocation: String, diagnostics: List<Diagnostic>?) {
    if (diagnostics.isNullOrEmpty()) return

    quickFixes[fileLocation] = Wrapper(diagnostics = diagnostics)

    onDiagnosticsUpdated?.invoke(fileLocation)

    val server = launcher?.remoteProxy

    diagnostics.forEach { diagnostic ->
        val context = CodeActionContext(listOf(diagnostic))

        server?.textDocumentService?.codeAction(
            CodeActionParams(
                TextDocumentIdentifier(fileLocation),
                diagnostic.range,
                context
            )
        )?.thenAccept { actions ->
            if (!actions.isNullOrEmpty()) {
                val existing = quickFixes[fileLocation]
                if (existing != null) {
                    quickFixes[fileLocation] = existing.copy(
                        fixes = (existing.fixes + actions).toMutableList()
                    )
                    onDiagnosticsUpdated?.invoke(fileLocation)
                }
            }
        }?.exceptionally { _ ->
            null
        }
    }
}

fun applyQuickFix(command: Command) {
    launcher?.remoteProxy?.workspaceService?.executeCommand(ExecuteCommandParams(command.command, command.arguments))
}
