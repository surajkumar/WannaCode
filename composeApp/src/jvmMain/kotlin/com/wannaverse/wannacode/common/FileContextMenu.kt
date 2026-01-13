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
    showPaste: Boolean = false,
    onDismiss: () -> Unit,
    onNewFile: () -> Unit = {},
    onNewFolder: () -> Unit = {},
    onRename: () -> Unit = {},
    onCut: () -> Unit = {},
    onCopy: () -> Unit = {},
    onPaste: () -> Unit = {},
    onDelete: () -> Unit = {}
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
        DropdownMenuItem(
            onClick = {
                onNewFile()
                onDismiss()
            },
            text = { Text(text = "New File", color = colors.menuText, fontSize = 14.sp) }
        )
        DropdownMenuItem(
            onClick = {
                onNewFolder()
                onDismiss()
            },
            text = { Text(text = "New Folder", color = colors.menuText, fontSize = 14.sp) }
        )
        DropdownMenuItem(
            onClick = {
                onRename()
                onDismiss()
            },
            text = { Text(text = "Rename", color = colors.menuText, fontSize = 14.sp) }
        )
        DropdownMenuItem(
            onClick = {
                onCut()
                onDismiss()
            },
            text = { Text(text = "Cut", color = colors.menuText, fontSize = 14.sp) }
        )
        DropdownMenuItem(
            onClick = {
                onCopy()
                onDismiss()
            },
            text = { Text(text = "Copy", color = colors.menuText, fontSize = 14.sp) }
        )
        if (showPaste) {
            DropdownMenuItem(
                onClick = {
                    onPaste()
                    onDismiss()
                },
                text = { Text(text = "Paste", color = colors.menuText, fontSize = 14.sp) }
            )
        }
        DropdownMenuItem(
            onClick = {
                onDelete()
                onDismiss()
            },
            text = { Text(text = "Delete", color = colors.error, fontSize = 14.sp) }
        )
    }
}

@Composable
fun FileContextMenu(
    file: File,
    contextMenuFile: File?,
    offset: DpOffset = DpOffset.Zero,
    onDismiss: () -> Unit,
    onRename: () -> Unit = {},
    onCut: () -> Unit = {},
    onCopy: () -> Unit = {},
    onDelete: () -> Unit = {}
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
        DropdownMenuItem(
            onClick = {
                onRename()
                onDismiss()
            },
            text = { Text(text = "Rename", color = colors.menuText, fontSize = 14.sp) }
        )
        DropdownMenuItem(
            onClick = {
                onCut()
                onDismiss()
            },
            text = { Text(text = "Cut", color = colors.menuText, fontSize = 14.sp) }
        )
        DropdownMenuItem(
            onClick = {
                onCopy()
                onDismiss()
            },
            text = { Text(text = "Copy", color = colors.menuText, fontSize = 14.sp) }
        )
        DropdownMenuItem(
            onClick = {
                onDelete()
                onDismiss()
            },
            text = { Text(text = "Delete", color = colors.error, fontSize = 14.sp) }
        )
    }
}
