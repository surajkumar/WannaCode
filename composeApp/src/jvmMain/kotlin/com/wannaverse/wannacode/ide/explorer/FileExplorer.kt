package com.wannaverse.wannacode.ide.explorer

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.SCROLL_BAR_IDLE_ACTIVE
import com.wannaverse.wannacode.SCROLL_BAR_IDLE_COLOR
import com.wannaverse.wannacode.STROKE_COLOR
import com.wannaverse.wannacode.ide.editor.CodeEditorViewModel
import com.wannaverse.wannacode.ide.explorer.components.FileTreeView
import java.awt.Cursor
import java.io.File

@Composable
fun FileExplorer(directory: File, viewModel: CodeEditorViewModel) {
    var sidebarWidth by remember { mutableStateOf(350.dp) }
    val scrollState = rememberScrollState()

    Box(Modifier.width(sidebarWidth).padding(start = 20.dp, top = 10.dp)) {
        Row {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(end = 8.dp)
            ) {
                FileTreeView(directory, viewModel = viewModel, refreshKey = viewModel.refreshKey)
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.width(8.dp),
                style = LocalScrollbarStyle.current.copy(
                    unhoverColor = SCROLL_BAR_IDLE_COLOR,
                    hoverColor = SCROLL_BAR_IDLE_ACTIVE,
                    minimalHeight = 24.dp
                )
            )
        }
    }

    Spacer(Modifier.width(5.dp))

    Box(
        Modifier
            .width(8.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    sidebarWidth = (sidebarWidth + dragAmount.x.dp).coerceIn(150.dp, 1000.dp)
                }
            }
            .fillMaxHeight()
            .background(Color.Transparent)
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
    ) {}

    Divider()
}

@Composable
fun Divider() {
    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(STROKE_COLOR)
    ) {}
}
