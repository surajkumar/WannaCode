package com.wannaverse.wannacode.ide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wannaverse.wannacode.ide.editor.CodeEditor
import com.wannaverse.wannacode.ide.editor.CodeEditorTabs
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.ide.explorer.FileExplorer
import com.wannaverse.wannacode.ide.terminal.Terminal
import com.wannaverse.wannacode.ide.terminal.TerminalViewModel
import com.wannaverse.wannacode.theme.WannaCodeTheme
import java.io.File

@Composable
fun IDEScreen(
    directory: File,
    viewModel: CodeEditorViewModel = viewModel { CodeEditorViewModel() },
    terminalViewModel: TerminalViewModel = viewModel { TerminalViewModel() },
    window: ComposeWindow? = null
) {
    val colors = WannaCodeTheme.colors
    viewModel.projectName.value = directory.name
    viewModel.directory.value = directory

    LaunchedEffect(directory) {
        terminalViewModel.workingDirectory = directory
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.background)
    ) {
        Toolbar(viewModel, terminalViewModel, window)

        Row(modifier = Modifier.weight(1f)) {
            FileExplorer(directory, viewModel)

            Column(modifier = Modifier.weight(1f)) {
                CodeEditorTabs(viewModel)
                CodeEditor(viewModel)
            }
        }

        Terminal(terminalViewModel)
    }
}
