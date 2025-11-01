package com.wannaverse.wannacode.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.io.File
import javax.swing.JFileChooser

@Composable
fun FolderSelector(onFolderSelected: (File) -> Unit, onCancel: () -> Unit) {
    LaunchedEffect(Unit) {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Select Folder"
            isAcceptAllFileFilterUsed = false
            currentDirectory = File(System.getProperty("user.home"))
        }

        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile?.let { onFolderSelected(it) }
        } else {
            onCancel()
        }
    }
}
