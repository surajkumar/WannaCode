package com.wannaverse.wannacode.ide.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.ERROR_RED
import com.wannaverse.wannacode.ide.editor.syntax.highlightCode
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.ide.editor.viewmodel.TabContent
import kotlin.math.min

@Composable
fun CodeEditorInput(tab: TabContent, viewModel: CodeEditorViewModel, onFontSizeChange: (TextUnit) -> Unit) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var fontSize by remember { mutableStateOf(14.sp) } // Dynamic font size
    val scrollSpeed = 2f

    val diagnostics by viewModel.diagnosticLineInfoList

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val scroll = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                        val isCtrlPressed = event.keyboardModifiers.isCtrlPressed
                        if (isCtrlPressed && scroll != 0f) {
                            fontSize = (fontSize.value - scroll * scrollSpeed).coerceIn(8f, 48f).sp
                            onFontSizeChange(fontSize)
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
    ) {
        BasicTextField(
            value = tab.text,
            onValueChange = { newText ->
                if (!tab.readOnly) viewModel.updateCurrentTabText(newText)
            },
            textStyle = TextStyle(
                color = Color.Transparent,
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                lineHeight = (fontSize.value * 1.7).sp
            ),
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Text(
                    text = highlightCode(code = tab.text, diagnostics = diagnostics),
                    style = TextStyle(
                        fontSize = fontSize,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = (fontSize.value * 1.7).sp
                    ),
                    onTextLayout = { textLayoutResult = it },
                    modifier = Modifier.drawBehind {
                        val layout = textLayoutResult ?: return@drawBehind
                        val annotatedText = layout.layoutInput.text
                        val textLength = annotatedText.length
                        if (textLength == 0) return@drawBehind

                        val annotations = annotatedText.getStringAnnotations(
                            tag = "diagnostic",
                            start = 0,
                            end = textLength
                        )

                        annotations.forEach { range ->
                            val start = range.start.coerceIn(0, textLength)
                            val end = range.end.coerceIn(0, textLength)
                            if (start >= end) return@forEach

                            applyErrorLine(
                                layout = layout,
                                start = start,
                                end = end,
                                waveLength = 8.dp.toPx(),
                                waveAmplitude = 2.dp.toPx(),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                )
                innerTextField()
            }
        )
    }
}

fun DrawScope.applyErrorLine(layout: TextLayoutResult, start: Int, end: Int, waveLength: Float, waveAmplitude: Float, strokeWidth: Float) {
    val startLine = layout.getLineForOffset(start)
    val endLine = layout.getLineForOffset(end - 1)

    for (line in startLine..endLine) {
        val lineStartOffset = maxOf(start, layout.getLineStart(line))
        val lineEndOffset = minOf(end, layout.getLineEnd(line))

        if (lineStartOffset >= lineEndOffset) continue

        val startBox = try {
            layout.getBoundingBox(lineStartOffset)
        } catch (_: Exception) {
            val left = layout.getLineLeft(line)
            val top = layout.getLineTop(line)
            val bottom = layout.getLineBottom(line)
            Rect(left, top, left + 1f, bottom)
        }

        val endBox = try {
            layout.getBoundingBox(lineEndOffset - 1)
        } catch (_: Exception) {
            val right = layout.getLineRight(line)
            val top = layout.getLineTop(line)
            val bottom = layout.getLineBottom(line)
            Rect(right - 1f, top, right, bottom)
        }

        val xStart = startBox.left
        val xEnd = endBox.right
        val y = layout.getLineBottom(line)

        val path = Path().apply {
            moveTo(xStart, y)
            var x = xStart
            var dir = 1f
            val halfWave = waveLength / 2f
            while (x < xEnd) {
                val nextX = min(x + halfWave, xEnd)
                lineTo(nextX, y + waveAmplitude * dir)
                dir *= -1f
                x = nextX
            }
        }

        drawPath(
            path = path,
            color = ERROR_RED,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
