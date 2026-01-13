package com.wannaverse.wannacode.ide.editor.virtualized

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.wannaverse.wannacode.ide.editor.viewmodel.DiagnosticLineInfo
import com.wannaverse.wannacode.ide.editor.viewmodel.FixArgument
import com.wannaverse.wannacode.theme.WannaCodeTheme

data class HoverInfo(
    val diagnostic: DiagnosticLineInfo,
    val xOffset: Float,
    val yOffset: Float
)

private class AbsolutePositionProvider(
    private val x: Int,
    private val y: Int
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val popupX = x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val popupY = y.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0))
        return IntOffset(popupX, popupY)
    }
}

@Composable
fun DiagnosticHoverPopup(
    hoverInfo: HoverInfo?,
    onDismiss: () -> Unit,
    onApplyFix: (FixArgument) -> Unit,
    modifier: Modifier = Modifier
) {
    if (hoverInfo == null) return

    val positionProvider = remember(hoverInfo.xOffset, hoverInfo.yOffset) {
        AbsolutePositionProvider(
            x = hoverInfo.xOffset.toInt(),
            y = hoverInfo.yOffset.toInt()
        )
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss
    ) {
        DiagnosticPopupContent(
            diagnostic = hoverInfo.diagnostic,
            onApplyFix = onApplyFix,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun DiagnosticPopupContent(
    diagnostic: DiagnosticLineInfo,
    onApplyFix: (FixArgument) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = WannaCodeTheme.colors
    val popupInteractionSource = remember { MutableInteractionSource() }
    val isPopupHovered by popupInteractionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .widthIn(min = 280.dp, max = 480.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .background(
                color = colors.menuBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(8.dp)
            )
            .hoverable(popupInteractionSource)
    ) {
        Column(
            modifier = Modifier.padding(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colors.diagnosticError.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠",
                        fontSize = 12.sp,
                        color = colors.diagnosticError
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Error",
                    color = colors.diagnosticError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = diagnostic.message,
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.Default
                )
            }

            if (diagnostic.fixes.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = colors.border
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔧",
                                fontSize = 10.sp
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Quick Fixes",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    diagnostic.fixes.forEach { fix ->
                        QuickFixItem(
                            fix = fix,
                            onClick = {
                                onApplyFix(fix)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickFixItem(
    fix: FixArgument,
    onClick: () -> Unit
) {
    val colors = WannaCodeTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val title = fix.changes.values.firstOrNull()?.firstOrNull()?.title ?: "Apply fix"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .background(
                color = if (isHovered) colors.menuHover else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = colors.accent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚡",
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
