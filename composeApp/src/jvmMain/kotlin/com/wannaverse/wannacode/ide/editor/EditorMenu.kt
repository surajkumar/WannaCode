package com.wannaverse.wannacode.ide.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.ide.editor.viewmodel.TabContent

@Composable
fun EditorMenu(
    tab: TabContent,
    fontSize: TextUnit,
    viewModel: CodeEditorViewModel
) {
    val color = Color.Gray
    var expandedMenu by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF18181C))
            .padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 10.dp)
        ) {
            MenuDropdown(
                title = "File",
                color = color,
                expandedMenu = expandedMenu,
                onMenuClick = { expandedMenu = if (expandedMenu == "File") null else "File" },
                items = listOf("New", "Open", "Save", "Save As")
            )

            MenuDropdown(
                title = "Edit",
                color = color,
                expandedMenu = expandedMenu,
                onMenuClick = { expandedMenu = if (expandedMenu == "Edit") null else "Edit" },
                items = listOf("Undo", "Redo", "Cut", "Copy", "Paste")
            )

            MenuDropdown(
                title = "View",
                color = color,
                expandedMenu = expandedMenu,
                onMenuClick = { expandedMenu = if (expandedMenu == "View") null else "View" },
                items = listOf("Zoom In", "Zoom Out", "Toggle Sidebar")
            )
        }
    }
}

@Composable
fun MenuDropdown(
    title: String,
    color: Color,
    expandedMenu: String?,
    onMenuClick: () -> Unit,
    items: List<String>
) {
    Box {
        Text(
            text = title,
            color = color,
            modifier = Modifier
                .clickable { onMenuClick() }
                .padding(4.dp)
        )
        DropdownMenu(
            expanded = expandedMenu == title,
            onDismissRequest = { onMenuClick() }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = color) },
                    onClick = {
                        onMenuClick()
                    }
                )
            }
        }
    }
}
