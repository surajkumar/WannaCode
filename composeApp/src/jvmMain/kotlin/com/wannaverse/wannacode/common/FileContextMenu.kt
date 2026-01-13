package com.wannaverse.wannacode.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.theme.WannaCodeTheme
import java.io.File

@Composable
fun DirectoryContextMenu(
    file: File,
    contextMenuFile: File?,
    offset: DpOffset = DpOffset.Zero,
    onDismiss: () -> Unit
) {
    val colors = WannaCodeTheme.colors

    DropdownMenu(
        expanded = contextMenuFile == file,
        onDismissRequest = onDismiss,
        offset = offset,
        modifier = Modifier
            .background(colors.menuBackground, shape = RoundedCornerShape(5.dp))
            .border(1.dp, colors.menuBorder, RoundedCornerShape(5.dp))
    ) {
        listOf("New File", "Rename", "Cut", "Copy", "Delete...").forEach { option ->
            DropdownMenuItem(
                onClick = {
                    onDismiss()
                },
                text = {
                    Text(
                        text = option,
                        color = colors.menuText,
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
    offset: DpOffset = DpOffset.Zero,
    onDismiss: () -> Unit
) {
    val colors = WannaCodeTheme.colors

    DropdownMenu(
        expanded = contextMenuFile == file,
        onDismissRequest = onDismiss,
        offset = offset,
        modifier = Modifier
            .background(colors.menuBackground, shape = RoundedCornerShape(5.dp))
            .border(1.dp, colors.menuBorder, RoundedCornerShape(5.dp))
    ) {
        listOf("Rename", "Cut", "Copy", "Delete...").forEach { option ->
            DropdownMenuItem(
                onClick = {
                    onDismiss()
                },
                text = {
                    Text(
                        text = option,
                        color = colors.menuText,
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}
