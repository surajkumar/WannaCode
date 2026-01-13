package com.wannaverse.wannacode.ide.explorer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.common.DirectoryContextMenu
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.theme.WannaCodeTheme
import java.io.File
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.down_cheveron
import wannacode.composeapp.generated.resources.folder
import wannacode.composeapp.generated.resources.right_cheveron

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DirectoryNode(file: File, indent: Int = 0, viewModel: CodeEditorViewModel, contextMenuFile: File?, onContextMenuFileChanged: (File?) -> Unit = {}, refreshKey: Int) {
    val colors = WannaCodeTheme.colors
    var expanded by remember { mutableStateOf(indent == 0) }
    var clickOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    Column(modifier = Modifier.padding(start = (indent * 8).dp)) {
        TooltipArea(
            tooltip = {
                Surface(
                    color = colors.tooltipBackground,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = file.name,
                        color = colors.tooltipText,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            },
            delayMillis = 500,
            tooltipPlacement = TooltipPlacement.CursorPoint(offset = DpOffset(10.dp, 10.dp))
        ) {
            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .pointerInput(file) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Press &&
                                    event.buttons.isSecondaryPressed
                                ) {
                                    val position = event.changes.first().position
                                    clickOffset = with(density) {
                                        DpOffset(position.x.toDp(), position.y.toDp())
                                    }
                                    onContextMenuFileChanged(file)
                                }
                            }
                        }
                    }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = if (expanded)
                        painterResource(Res.drawable.down_cheveron)
                    else painterResource(Res.drawable.right_cheveron),
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = colors.explorerChevron,
                    modifier = Modifier.size(10.dp)
                )

                Spacer(Modifier.width(4.dp))

                Icon(
                    painter = painterResource(Res.drawable.folder),
                    contentDescription = null,
                    tint = colors.explorerIcon,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = file.name,
                    color = colors.explorerText
                )

                DirectoryContextMenu(file, contextMenuFile, clickOffset) { onContextMenuFileChanged(null) }
            }
        }

        if (expanded) {
            file.listFiles()?.sortedBy { !it.isDirectory }?.forEach {
                Spacer(Modifier.height(2.dp))
                FileTreeView(it, indent + 1, viewModel, refreshKey)
            }
        }
    }
}
