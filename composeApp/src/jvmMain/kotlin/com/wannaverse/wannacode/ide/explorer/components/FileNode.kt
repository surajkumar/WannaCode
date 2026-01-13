package com.wannaverse.wannacode.ide.explorer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.common.FileContextMenu
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.theme.WannaCodeTheme
import java.io.File
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.file

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileNode(file: File, indent: Int = 0, viewModel: CodeEditorViewModel, contextMenuFile: File?, onContextMenuFileChanged: (File?) -> Unit = {}) {
    val colors = WannaCodeTheme.colors
    var clickOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = androidx.compose.ui.platform.LocalDensity.current

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
        delayMillis = 100,
        tooltipPlacement = TooltipPlacement.CursorPoint(offset = DpOffset(10.dp, 10.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(start = (indent * 8).dp)
                .clickable { viewModel.openFile(file) }
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
            Spacer(Modifier.width(14.dp))

            val extension = file.extension.lowercase()
            val badge = fileBadges[extension]

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(badge.color, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.letter,
                        color = colors.textPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }
            } else {
                Icon(
                    painter = painterResource(Res.drawable.file),
                    contentDescription = null,
                    tint = colors.explorerIcon,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(6.dp))

            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = file.name,
                color = colors.explorerText
            )

            FileContextMenu(file, contextMenuFile, clickOffset) { onContextMenuFileChanged(null) }
        }
    }
}
