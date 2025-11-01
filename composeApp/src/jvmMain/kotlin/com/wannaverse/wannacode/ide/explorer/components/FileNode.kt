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
import java.io.File
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.file

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileNode(file: File, indent: Int = 0, viewModel: CodeEditorViewModel, contextMenuFile: File?, onContextMenuFileChanged: (File?) -> Unit = {}) {
    TooltipArea(
        tooltip = {
            Surface(
                color = Color(0xFF2B2B2B),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = file.name,
                    color = Color.White,
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
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press &&
                                event.buttons.isSecondaryPressed
                            ) {
                                onContextMenuFileChanged(file)
                            }
                        }
                    }
                }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }
            } else {
                Icon(
                    painter = painterResource(Res.drawable.file),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(5.dp))

            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = file.name,
                modifier = Modifier.clickable {
                    viewModel.openFile(file)
                }
            )
        }

        FileContextMenu(file, contextMenuFile) { onContextMenuFileChanged(null) }
    }
}
