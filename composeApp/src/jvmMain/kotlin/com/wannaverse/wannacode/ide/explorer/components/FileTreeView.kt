package com.wannaverse.wannacode.ide.explorer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import java.io.File
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTreeView(file: File, indent: Int = 0, viewModel: CodeEditorViewModel, refreshKey: Int = 0) {
    var contextMenuFile by remember { mutableStateOf<File?>(null) }

    if (file.isDirectory && indent == 0) {
        LaunchedEffect(file, refreshKey) {
            var previousSnapshot = file.listFiles()?.map { it.name to it.lastModified() }?.toSet() ?: emptySet()

            while (true) {
                delay(2000)

                val currentSnapshot = file.listFiles()?.map { it.name to it.lastModified() }?.toSet() ?: emptySet()
                if (currentSnapshot != previousSnapshot) {
                    previousSnapshot = currentSnapshot
                    viewModel.refreshTree()
                }
            }
        }
    }

    if (file.isDirectory) {
        DirectoryNode(
            file = file,
            indent = indent,
            viewModel = viewModel,
            contextMenuFile = contextMenuFile,
            onContextMenuFileChanged = {
                contextMenuFile = it
            },
            refreshKey = refreshKey
        )
    } else {
        FileNode(
            file = file,
            indent = indent,
            viewModel = viewModel,
            contextMenuFile = contextMenuFile,
            onContextMenuFileChanged = {
                contextMenuFile = it
            }
        )
    }
}
