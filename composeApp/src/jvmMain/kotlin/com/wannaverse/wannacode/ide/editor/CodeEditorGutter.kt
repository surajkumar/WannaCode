package com.wannaverse.wannacode.ide.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CodeEditorGutter(
    tab: TabContent,
    fontSize: TextUnit,
    viewModel: CodeEditorViewModel
) {
    val diagnostics by viewModel.diagnosticLineInfoList
    val diagnosticsByLine = diagnostics.groupBy { it.diagnosticLine }

    Column(
        modifier = Modifier
            .padding(end = 8.dp)
            .background(Color(0xFF17171D))
            .padding(start = 5.dp, end = 10.dp)
            .fillMaxHeight()
    ) {
        tab.text.split("\n").forEachIndexed { index, _ ->
            val diagsForLine = diagnosticsByLine[index] ?: emptyList()

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Line number with dynamic fontSize
                Text(
                    text = "${index + 1}",
                    color = Color(0xFF383838),
                    fontSize = fontSize,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = (fontSize.value * 1.7).sp
                )

                if (diagsForLine.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    var isHovered by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .size((fontSize.value * 0.6).dp)
                            .background(Color.Cyan, shape = CircleShape)
                            .pointerMoveFilter(
                                onEnter = {
                                    isHovered = true
                                    false
                                },
                                onExit = { false }
                            )
                    ) {
                        if (isHovered) {
                            Popup(
                                alignment = Alignment.TopStart,
                                offset = IntOffset(12, 0),
                                properties = PopupProperties(focusable = false)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .background(Color(0xFF2D2D2D), RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                        .widthIn(max = 300.dp)
                                        .pointerMoveFilter(
                                            onEnter = { false },
                                            onExit = {
                                                isHovered = false
                                                false
                                            }
                                        )
                                ) {
                                    diagsForLine.forEach { diag ->
                                        Text(
                                            text = diag.message,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        diag.fixes
                                            .flatMap { fix -> fix.changes.flatMap { it.value } }
                                            .take(3)
                                            .forEach { change ->
                                                if (change.title?.contains("Javadoc comment") != true) {
                                                    Text(
                                                        text = change.title ?: "",
                                                        color = Color(0xFF00FF00),
                                                        fontSize = 12.sp,
                                                        modifier = Modifier.clickable {
                                                            viewModel.applyFix(change)
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
