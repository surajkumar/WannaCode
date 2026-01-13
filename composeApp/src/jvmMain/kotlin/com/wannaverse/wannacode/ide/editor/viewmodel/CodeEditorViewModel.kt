package com.wannaverse.wannacode.ide.editor.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.wannaverse.wannacode.ide.editor.jdt.applyQuickFix
import com.wannaverse.wannacode.ide.editor.jdt.getDiagnostics
import com.wannaverse.wannacode.ide.editor.jdt.launchJdtServer
import com.wannaverse.wannacode.ide.editor.jdt.onDiagnosticsUpdated
import com.wannaverse.wannacode.ide.editor.jdt.quickFixes
import com.wannaverse.wannacode.ide.editor.virtualized.VirtualizedEditorState
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.mutableListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.lsp4j.WorkspaceEdit

class CodeEditorViewModel : ViewModel() {
    private var nextId = 0
        get() = field++
    private val activeTabTitles = mutableStateListOf<String>()
    private val _tabContents = mutableStateMapOf<Int, TabContent>()
    val tabContents: Map<Int, TabContent> = _tabContents
    val currentTab = mutableStateOf(-1)
    val projectName = mutableStateOf("")
    val directory = mutableStateOf(File(""))
    private val tabDiagnostics = mutableStateMapOf<Int, MutableState<List<DiagnosticLineInfo>>>()
    val diagnosticLineInfoList: State<List<DiagnosticLineInfo>>
        get() = tabDiagnostics.getOrPut(currentTab.value) { mutableStateOf(emptyList()) }
    var refreshKey by mutableStateOf(0)
        private set
    private val tabLogs = mutableStateMapOf<Int, MutableState<List<String>>>()
    private val tabTexts = mutableStateMapOf<Int, MutableStateFlow<String>>()
    private var saveJob: Job? = null
    private val gson = Gson()

    private val editorStates = mutableStateMapOf<Int, VirtualizedEditorState>()

    init {
        onDiagnosticsUpdated = { fileUri ->
            val matchingTab = _tabContents.values.find { tab ->
                val tabUri = "file:///" + tab.file.absolutePath.replace("\\", "/")
                tabUri == fileUri || "file://" + tab.file.absolutePath.replace("\\", "/") == fileUri
            }
            matchingTab?.let { tab ->
                viewModelScope.launch(Dispatchers.Main) {
                    populateQuickFixesByLine(tab.id, fileUri)
                }
            }
        }
    }

    fun addLog(msg: String, tabId: Int = currentTab.value) {
        val state = tabLogs.getOrPut(tabId) { mutableStateOf(emptyList()) }
        state.value += msg
    }

    fun clearLogs(tabId: Int = currentTab.value) {
        tabLogs[tabId]?.value = emptyList()
    }

    fun getLogs(tabId: Int = currentTab.value): State<List<String>> = tabLogs.getOrPut(tabId) { mutableStateOf(emptyList()) }

    fun refreshTree() {
        refreshKey++
    }

    fun startJdtServer(project: File) {
        launchJdtServer(workspace = File(project.parent))
    }

    fun loadDiagnostics(tab: TabContent) = viewModelScope.launch {
        getDiagnostics(tab.file, tab.text)
    }

    fun getDiagnostics(file: File, content: String) = getDiagnostics(
        code = content,
        fileLocation = file.absolutePath
    )

    fun applyFix(change: Change) {
        val tabId = currentTab.value
        val tab = _tabContents[tabId] ?: return
        var newText = tab.text

        change.range.let { range ->
            val lines = newText.lines().toMutableList()
            val startLine = range.start.line
            val startChar = range.start.character
            val endLine = range.end.line
            val endChar = range.end.character
            val before = lines[startLine].substring(0, startChar)
            val after = lines[endLine].substring(endChar)
            val newLines = change.newText.split("\n")

            lines.subList(startLine, endLine + 1).clear()
            lines.addAll(startLine, newLines)
            lines[startLine] = before + lines[startLine]
            lines[startLine + newLines.size - 1] += after
            newText = lines.joinToString("\n")
        }

        updateCurrentTabText(newText)

        change.command?.let { command ->
            if (command.command == "java.apply.workspaceEdit") {
                // Parse the workspace edit from command arguments
                val gson = Gson()
                val editJson = gson.toJson(command.arguments?.get(0))
                val workspaceEdit = gson.fromJson(editJson, WorkspaceEdit::class.java)
                applyWorkspaceEdit(workspaceEdit)
            } else {
                applyQuickFix(command)
            }
        }
    }

    fun applyFixArgument(fixArg: FixArgument) {
        val tabId = currentTab.value
        val tab = _tabContents[tabId] ?: return
        val fileUri = "file://" + tab.file.absolutePath.replace("\\", "/")
        val changes = fixArg.changes[fileUri]
            ?: fixArg.changes["file:///" + tab.file.absolutePath.replace("\\", "/")]
            ?: fixArg.changes.values.firstOrNull()
            ?: return

        changes.sortedByDescending { it.range.start.line * 10000 + it.range.start.character }
            .forEach { change ->
                applyFix(change)
            }

        loadDiagnostics(tab)

        clearEditorState(tabId)
    }

    fun applyWorkspaceEdit(edit: WorkspaceEdit) {
        edit.changes?.forEach { (uri, textEdits) ->
            val filePath = uri.removePrefix("file://").replace("/", File.separator)
            val file = File(filePath)
            if (!file.exists()) return@forEach

            var code = file.readText()
            textEdits.sortedByDescending { it.range.start.line * 1000 + it.range.start.character }.forEach { te ->
                val lines = code.lines().toMutableList()
                val startLine = te.range.start.line
                val startChar = te.range.start.character
                val endLine = te.range.end.line
                val endChar = te.range.end.character
                val before = lines[startLine].substring(0, startChar)
                val after = lines[endLine].substring(endChar)
                val newLines = te.newText.split("\n")

                lines.subList(startLine, endLine + 1).clear()
                lines.addAll(startLine, newLines)
                lines[startLine] = before + lines[startLine]
                lines[startLine + newLines.size - 1] += after
                code = lines.joinToString("\n")
            }
            file.writeText(code)
        }
    }

    fun populateQuickFixesByLine(tabId: Int, file: String) {
        val newList = mutableListOf<DiagnosticLineInfo>()

        val diagnostics = quickFixes[file]?.diagnostics ?: return
        val fixes = quickFixes[file]?.fixes ?: return

        val fixArgumentsList = mutableListOf<FixArgument>()

        fixes.forEach { fix ->
            val command = fix.left
            val arguments = command.arguments
            val title = command.title

            arguments.forEach { arg ->
                if (arg is JsonObject) {
                    val jsonString = gson.toJson(arg)
                    val fixArg = gson.fromJson(jsonString, FixArgument::class.java)

                    fixArg.changes.values.flatten().forEach { change ->
                        change.title = title
                        change.command = command
                    }

                    fixArgumentsList.add(fixArg)
                }
            }
        }

        diagnostics.forEach { diag ->
            val msg = "Line ${diag.range.start.line}: ${diag.message}"
            addLog(msg, tabId)

            newList.add(
                DiagnosticLineInfo(
                    diagnosticLine = diag.range.start.line,
                    startChar = diag.range.start.character,
                    endChar = diag.range.end.character,
                    message = diag.message,
                    fixes = fixArgumentsList
                )
            )
        }

        tabDiagnostics[tabId] = mutableStateOf(newList)
    }

    val orderedTabIds: List<Int>
        get() = activeTabTitles.mapNotNull { title ->
            _tabContents.entries.find { it.value.file.name == title }?.key
        }

    fun showTab(tabId: Int) {
        if (tabId in _tabContents) {
            currentTab.value = tabId
        }
    }

    fun closeTab(tabId: Int) {
        val tab = _tabContents[tabId] ?: return

        activeTabTitles.remove(tab.file.name)
        _tabContents.remove(tabId)
        tabLogs.remove(tabId)
        tabDiagnostics.remove(tabId)
        editorStates.remove(tabId)

        if (currentTab.value == tabId) {
            currentTab.value = activeTabTitles.firstOrNull()?.let { title ->
                _tabContents.entries.find { it.value.file.name == title }?.key
            } ?: -1
        }
    }

    fun openFile(file: File) {
        if (!file.isFile) return

        val existingTab = _tabContents.values.find { it.file == file }
        if (existingTab != null) {
            showTab(existingTab.id)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = file.toPath().readTextSafe()

                withContext(Dispatchers.Main) {
                    val tabId = nextId
                    val title = file.name

                    activeTabTitles.add(title)
                    _tabContents[tabId] = TabContent(
                        id = tabId,
                        file = file,
                        text = content,
                        readOnly = file.toPath().isLikelyBinary()
                    )

                    tabLogs[tabId] = mutableStateOf(emptyList())
                    tabDiagnostics[tabId] = mutableStateOf(emptyList())

                    showTab(tabId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun Path.readTextSafe(
        charset: Charset = Charsets.UTF_8,
        replacement: Char = '�'
    ): String {
        if (isLikelyBinary()) {
            return "<binary file - cannot display as text>"
        }

        val bytes = Files.readAllBytes(this)
        val decoder = charset.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
            .replaceWith(replacement.toString())

        return decoder.decode(ByteBuffer.wrap(bytes)).toString()
    }

    fun Path.isLikelyBinary(previewSize: Long = 1024): Boolean {
        if (!Files.isRegularFile(this)) return true
        if (Files.size(this) == 0L) return false

        val sizeToCheck = minOf(Files.size(this), previewSize)
        val buffer = ByteArray(sizeToCheck.toInt())

        Files.newInputStream(this).use { stream ->
            val read = stream.read(buffer, 0, buffer.size)
            if (read <= 0) return false
            var nullCount = 0
            var controlCount = 0
            for (i in 0 until read) {
                val b = buffer[i].toInt() and 0xFF
                when {
                    b == 0 -> nullCount++
                    b < 32 && b !in setOf(9, 10, 13) -> controlCount++
                }
            }
            val controlRatio = controlCount.toDouble() / read
            return nullCount > 0 || controlRatio > 0.05
        }
    }

    fun updateCurrentTabText(newText: String) {
        val id = currentTab.value
        val tab = _tabContents[id] ?: return
        _tabContents[id] = tab.copy(text = newText, isDirty = true)
        tabTexts[id]?.value = newText
        debounceSave(tab.file, newText)
    }

    fun getEditorState(tabId: Int): VirtualizedEditorState = editorStates.getOrPut(tabId) {
        val tab = _tabContents[tabId]
        VirtualizedEditorState(
            initialText = tab?.text ?: "",
            readOnly = tab?.readOnly ?: false
        )
    }

    fun syncEditorState(tabId: Int) {
        val state = editorStates[tabId] ?: return
        val tab = _tabContents[tabId] ?: return
        val newText = state.document.getText()
        if (tab.text != newText) {
            _tabContents[tabId] = tab.copy(text = newText, isDirty = true)
            debounceSave(tab.file, newText)
        }
    }

    fun clearEditorState(tabId: Int) {
        editorStates.remove(tabId)
    }

    private fun debounceSave(file: File, text: String) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            saveFile(file, text)
        }
    }

    fun saveFile(file: File, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                file.writeText(content)

                withContext(Dispatchers.Main) {
                    val tab = _tabContents.values.find { it.file == file } ?: return@withContext
                    _tabContents[tab.id] = tab.copy(isDirty = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var clipboardFile: File? = null
    private var clipboardOperation: ClipboardOperation? = null

    enum class ClipboardOperation { CUT, COPY }

    data class PasteConflict(
        val source: File,
        val destination: File,
        val targetDirectory: File,
        val operation: ClipboardOperation
    )

    var pendingPasteConflict by mutableStateOf<PasteConflict?>(null)
        private set

    fun cutFile(file: File) {
        clipboardFile = file
        clipboardOperation = ClipboardOperation.CUT
    }

    fun copyFile(file: File) {
        clipboardFile = file
        clipboardOperation = ClipboardOperation.COPY
    }

    fun pasteFile(targetDirectory: File): Boolean {
        val source = clipboardFile ?: return false
        val operation = clipboardOperation ?: return false
        val destination = File(targetDirectory, source.name)

        if (destination.exists()) {
            pendingPasteConflict = PasteConflict(source, destination, targetDirectory, operation)
            return false
        }

        executePaste(source, destination, operation)
        return true
    }

    fun resolveConflictReplace() {
        val conflict = pendingPasteConflict ?: return
        pendingPasteConflict = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (conflict.destination.isDirectory) {
                    conflict.destination.deleteRecursively()
                } else {
                    conflict.destination.delete()
                }

                withContext(Dispatchers.Main) {
                    executePaste(conflict.source, conflict.destination, conflict.operation)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resolveConflictKeepBoth() {
        val conflict = pendingPasteConflict ?: return
        pendingPasteConflict = null

        val newName = generateUniqueName(conflict.source.name, conflict.targetDirectory)
        val newDestination = File(conflict.targetDirectory, newName)
        executePaste(conflict.source, newDestination, conflict.operation)
    }

    fun resolveConflictSkip() {
        pendingPasteConflict = null
    }

    private fun generateUniqueName(originalName: String, targetDirectory: File): String {
        val dotIndex = originalName.lastIndexOf('.')
        val baseName = if (dotIndex > 0) originalName.substring(0, dotIndex) else originalName
        val extension = if (dotIndex > 0) originalName.substring(dotIndex) else ""

        var counter = 1
        var newName: String
        do {
            newName = "$baseName ($counter)$extension"
            counter++
        } while (File(targetDirectory, newName).exists())

        return newName
    }

    private fun executePaste(source: File, destination: File, operation: ClipboardOperation) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (source.isDirectory) {
                    source.copyRecursively(destination)
                    if (operation == ClipboardOperation.CUT) {
                        source.deleteRecursively()
                    }
                } else {
                    source.copyTo(destination)
                    if (operation == ClipboardOperation.CUT) {
                        source.delete()
                    }
                }

                if (operation == ClipboardOperation.CUT) {
                    clipboardFile = null
                    clipboardOperation = null
                }

                withContext(Dispatchers.Main) {
                    refreshTree()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createNewFile(parentDirectory: File, fileName: String): Boolean {
        if (fileName.isBlank()) return false

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newFile = File(parentDirectory, fileName)
                if (newFile.exists()) {
                    return@launch
                }
                newFile.createNewFile()

                withContext(Dispatchers.Main) {
                    refreshTree()
                    openFile(newFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    fun createNewFolder(parentDirectory: File, folderName: String): Boolean {
        if (folderName.isBlank()) return false

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newFolder = File(parentDirectory, folderName)
                if (newFolder.exists()) {
                    return@launch
                }
                newFolder.mkdir()

                withContext(Dispatchers.Main) {
                    refreshTree()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    fun renameFile(file: File, newName: String): Boolean {
        if (newName.isBlank()) return false

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newFile = File(file.parentFile, newName)
                if (newFile.exists()) {
                    return@launch
                }

                val success = file.renameTo(newFile)
                if (success) {
                    withContext(Dispatchers.Main) {
                        // Update root directory if it was renamed
                        if (file == directory.value) {
                            directory.value = newFile
                            projectName.value = newFile.name
                        }

                        // Update any open tabs with this file
                        val tab = _tabContents.values.find { it.file == file }
                        if (tab != null) {
                            activeTabTitles.remove(tab.file.name)
                            activeTabTitles.add(newFile.name)
                            _tabContents[tab.id] = tab.copy(file = newFile)
                        }
                        refreshTree()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    fun deleteFile(file: File): Boolean {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Close any open tabs for this file
                withContext(Dispatchers.Main) {
                    val tab = _tabContents.values.find { it.file == file }
                    if (tab != null) {
                        closeTab(tab.id)
                    }
                }

                if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }

                withContext(Dispatchers.Main) {
                    refreshTree()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    fun hasClipboard(): Boolean = clipboardFile != null
}
