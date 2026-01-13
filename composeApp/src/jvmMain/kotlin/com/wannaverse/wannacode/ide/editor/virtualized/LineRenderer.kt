package com.wannaverse.wannacode.ide.editor.virtualized

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.wannaverse.wannacode.ERROR_RED
import com.wannaverse.wannacode.ide.editor.viewmodel.DiagnosticLineInfo

@Composable
fun LineRenderer(
    lineIndex: Int,
    lineContent: String,
    state: VirtualizedEditorState,
    highlightedText: AnnotatedString,
    diagnostics: List<DiagnosticLineInfo>,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val lineHeightDp = with(density) { state.lineHeightPx.toDp() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(lineHeightDp)
            .drawBehind {
                state.selection?.let { selection ->
                    drawSelectionForLine(
                        selection = selection,
                        lineIndex = lineIndex,
                        lineLength = lineContent.length,
                        charWidth = state.charWidthPx,
                        lineHeight = state.lineHeightPx
                    )
                }

                drawDiagnosticUnderlines(
                    diagnostics = diagnostics,
                    charWidth = state.charWidthPx,
                    lineHeight = state.lineHeightPx,
                    lineLength = lineContent.length
                )
            }
    ) {
        Text(
            text = highlightedText,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            softWrap = false
        )

        if (state.cursor.line == lineIndex) {
            CursorIndicator(
                column = state.cursor.column,
                charWidth = state.charWidthPx,
                lineHeight = state.lineHeightPx
            )
        }
    }
}

@Composable
private fun CursorIndicator(
    column: Int,
    charWidth: Float,
    lineHeight: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(530),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val x = column * charWidth
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = Offset(x, 2f),
            end = Offset(x, lineHeight - 2f),
            strokeWidth = 2f
        )
    }
}

fun DrawScope.drawSelectionForLine(
    selection: SelectionRange,
    lineIndex: Int,
    lineLength: Int,
    charWidth: Float,
    lineHeight: Float,
    selectionColor: Color = Color(0xFF264F78)
) {
    val bounds = selection.getBoundsForLine(lineIndex, lineLength) ?: return
    val (startCol, endCol) = bounds

    if (startCol >= endCol && lineIndex == selection.end.line && lineIndex == selection.start.line) return

    val startX = startCol * charWidth
    val endX = if (lineIndex < selection.end.line) {
        (lineLength * charWidth) + charWidth * 0.5f
    } else {
        endCol * charWidth
    }

    if (endX > startX) {
        drawRect(
            color = selectionColor,
            topLeft = Offset(startX, 0f),
            size = Size(endX - startX, lineHeight)
        )
    }
}

private fun DrawScope.drawDiagnosticUnderlines(
    diagnostics: List<DiagnosticLineInfo>,
    charWidth: Float,
    lineHeight: Float,
    lineLength: Int
) {
    diagnostics.forEach { diagnostic ->
        val start = diagnostic.startChar.coerceIn(0, lineLength)
        val end = diagnostic.endChar.coerceIn(0, lineLength)

        if (start < end) {
            val startX = start * charWidth
            val endX = end * charWidth
            val y = lineHeight - 4f

            drawWavyLine(
                startX = startX,
                endX = endX,
                y = y,
                color = ERROR_RED,
                waveLength = 4f,
                waveAmplitude = 2f
            )
        }
    }
}

private fun DrawScope.drawWavyLine(
    startX: Float,
    endX: Float,
    y: Float,
    color: Color,
    waveLength: Float,
    waveAmplitude: Float
) {
    val path = Path()
    path.moveTo(startX, y)

    var x = startX
    var direction = 1
    while (x < endX) {
        val nextX = (x + waveLength).coerceAtMost(endX)
        val controlY = y + (direction * waveAmplitude)
        path.quadraticTo(
            (x + nextX) / 2f,
            controlY,
            nextX,
            y
        )
        x = nextX
        direction *= -1
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
    )
}
