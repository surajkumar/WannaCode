package com.wannaverse.wannacode.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun DirectoryContextMenu(
    file: File,
    contextMenuFile: File?,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = contextMenuFile == file,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(Color(0xFF111114), shape = RoundedCornerShape(5.dp))
            .border(1.dp, Color(0xFF252525), RoundedCornerShape(5.dp))
    ) {
        listOf("New File", "Rename", "Cut", "Copy", "Delete...").forEach { option ->
            DropdownMenuItem(
                onClick = {
                    onDismiss()
                },
                text = {
                    Text(
                        text = option,
                        color = Color(0XFFB6B6B6),
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}

@Composable
fun FileContextMenu(
    file: File,
    contextMenuFile: File?,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = contextMenuFile == file,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(Color(0xFF111114), shape = RoundedCornerShape(5.dp))
            .border(1.dp, Color(0xFF252525), RoundedCornerShape(5.dp))
    ) {
        listOf("Rename", "Cut", "Copy", "Delete...").forEach { option ->
            DropdownMenuItem(
                onClick = {
                    onDismiss()
                },
                text = {
                    Text(
                        text = option,
                        color = Color(0XFFB6B6B6),
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}
