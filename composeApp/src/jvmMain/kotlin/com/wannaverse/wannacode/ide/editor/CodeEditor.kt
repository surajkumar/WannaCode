package com.wannaverse.wannacode.ide.editor

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.common.Scrollbar
import com.wannaverse.wannacode.ide.editor.panel.InfoPanel
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CodeEditor(viewModel: CodeEditorViewModel) {
    val currentTabId by viewModel.currentTab

    val tab = currentTabId.takeIf { it != -1 }
        ?.let { viewModel.tabContents[it] }
        ?: return

    val scrollState = rememberScrollState()

    LaunchedEffect(tab.text) {
        if (tab.file.name.endsWith(".java")) {
            viewModel.loadDiagnostics(tab)
        }
    }

    var fontSize by remember { mutableStateOf(14.sp) }

    Column(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val maxHeight = this.maxHeight

            Row(
                modifier = Modifier
                    .heightIn(max = maxHeight)
                    .verticalScroll(scrollState)
            ) {
                CodeEditorGutter(tab, viewModel = viewModel, fontSize = fontSize)
                CodeEditorInput(tab, viewModel = viewModel, onFontSizeChange = {
                    fontSize = it
                })
            }

            Scrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
        }
    }
}
