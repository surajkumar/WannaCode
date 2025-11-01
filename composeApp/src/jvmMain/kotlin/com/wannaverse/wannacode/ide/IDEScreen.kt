package com.wannaverse.wannacode.ide

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wannaverse.wannacode.ide.editor.CodeEditor
import com.wannaverse.wannacode.ide.editor.CodeEditorTabs
import com.wannaverse.wannacode.ide.editor.CodeEditorViewModel
import com.wannaverse.wannacode.ide.explorer.FileExplorer
import com.wannaverse.wannacode.ide.terminal.Terminal
import java.io.File

@Composable
fun IDEScreen(directory: File, viewModel: CodeEditorViewModel = viewModel { CodeEditorViewModel() }) {
    viewModel.projectName.value = directory.name
    viewModel.directory.value = directory

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Toolbar(viewModel)

        Row(modifier = Modifier.weight(1f)) {
            FileExplorer(directory, viewModel)

            Column {
                CodeEditorTabs(viewModel)
                CodeEditor(viewModel)
            }
        }

        Column {
            Terminal()
        }
    }
}
