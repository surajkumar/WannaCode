package com.wannaverse.wannacode.ide.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.ide.editor.panel.InfoPanel
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.ide.editor.virtualized.VirtualizedCodeEditor

@Composable
fun CodeEditor(viewModel: CodeEditorViewModel) {
    val currentTabId by viewModel.currentTab

    val tab = currentTabId.takeIf { it != -1 }
        ?.let { viewModel.tabContents[it] }
        ?: return

    val diagnostics by viewModel.diagnosticLineInfoList

    LaunchedEffect(tab.text) {
        if (tab.file.name.endsWith(".java")) {
            viewModel.loadDiagnostics(tab)
        }
    }

    val editorState = remember(currentTabId) {
        viewModel.getEditorState(currentTabId)
    }

    // Determine language from file extension
    val language = tab.file.name.substringAfterLast('.')

    var fontSize by remember { mutableStateOf(14f) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            VirtualizedCodeEditor(
                state = editorState,
                language = language,
                diagnostics = diagnostics,
                fontSize = fontSize,
                onTextChange = { newText ->
                    viewModel.syncEditorState(currentTabId)
                },
                onApplyFix = { fixArg ->
                    viewModel.applyFixArgument(fixArg)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        InfoPanel(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
    }
}
