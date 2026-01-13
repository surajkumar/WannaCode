package com.wannaverse.wannacode.ide.editor.virtualized

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.ide.editor.syntax.loadSyntaxMap
import com.wannaverse.wannacode.ide.editor.viewmodel.DiagnosticLineInfo
import com.wannaverse.wannacode.ide.editor.viewmodel.FixArgument
import com.wannaverse.wannacode.theme.WannaCodeTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VirtualizedCodeEditor(
    state: VirtualizedEditorState,
    language: String,
    diagnostics: List<DiagnosticLineInfo>,
    modifier: Modifier = Modifier,
    fontSize: Float = 14f,
    onTextChange: (String) -> Unit,
    onApplyFix: ((FixArgument) -> Unit)? = null
) {
    val colors = WannaCodeTheme.colors
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val focusRequester = remember(state) { FocusRequester() }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var hoverInfo by remember { mutableStateOf<HoverInfo?>(null) }
    var hoverJob by remember { mutableStateOf<Job?>(null) }
    val hoverDelayMs = 300L

    val textStyle = remember(fontSize, colors.textPrimary) {
        TextStyle(
            fontSize = fontSize.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = (fontSize * 1.5f).sp,
            color = colors.textPrimary
        )
    }

    LaunchedEffect(fontSize, state) {
        val measured = textMeasurer.measure("M", textStyle)
        state.lineHeightPx = measured.size.height.toFloat()
        state.charWidthPx = measured.size.width.toFloat()
        state.textMeasurer = textMeasurer
    }

    val syntaxCache = remember(language) {
        try {
            val syntaxMap = loadSyntaxMap("syntax_map.json")
            val syntax = syntaxMap[language]
            syntax?.let { LineSyntaxCache(it) }
        } catch (e: Exception) {
            null
        }
    }

    val diagnosticsByLine = remember(diagnostics) {
        diagnostics.groupBy { it.diagnosticLine }
    }

    LaunchedEffect(state.cursor) {
        state.scrollToCursor()
    }

    val gutterWidthDp = with(density) {
        val maxDigits = state.document.lineCount.toString().length.coerceAtLeast(3)
        val charWidth = state.charWidthPx.toDp()
        val textWidth = charWidth * maxDigits
        textWidth + 24.dp
    }

    fun calculatePosition(x: Float, y: Float): CursorPosition {
        val lineHeight = state.lineHeightPx.takeIf { it > 0f } ?: 24f
        val charWidth = state.charWidthPx.takeIf { it > 0f } ?: 8f

        val scrollOffset = state.scrollState.firstVisibleItemIndex
        val scrollPixelOffset = state.scrollState.firstVisibleItemScrollOffset

        val adjustedY = y + scrollPixelOffset
        val lineIndex = (adjustedY / lineHeight).toInt() + scrollOffset
        val clampedLine = lineIndex.coerceIn(0, (state.document.lineCount - 1).coerceAtLeast(0))

        val column = (x / charWidth).toInt()
        val clampedColumn = column.coerceIn(0, state.document.getLineLength(clampedLine))

        return CursorPosition(clampedLine, clampedColumn)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.editorBackground)
            .onSizeChanged { size ->
                val lineHeight = state.lineHeightPx.takeIf { it > 0 } ?: 24f
                state.visibleLineCount = (size.height / lineHeight).toInt().coerceAtLeast(1)
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = state.scrollState,
                modifier = Modifier
                    .width(gutterWidthDp)
                    .background(colors.editorGutter)
                    .padding(horizontal = 8.dp),
                userScrollEnabled = false
            ) {
                itemsIndexed(
                    items = state.document.lines,
                    key = { index, _ -> "gutter_$index" }
                ) { lineIndex, _ ->
                    val lineHeightDp = with(density) { state.lineHeightPx.toDp() }
                    val maxDigits = state.document.lineCount.toString().length.coerceAtLeast(3)
                    val lineNumber = (lineIndex + 1).toString().padStart(maxDigits)
                    val isCurrentLine = state.cursor.line == lineIndex

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(lineHeightDp)
                    ) {
                        Text(
                            text = lineNumber,
                            style = textStyle.copy(
                                color = if (isCurrentLine) colors.editorLineNumberActive else colors.editorLineNumber
                            )
                        )

                        if (diagnosticsByLine.containsKey(lineIndex)) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .width(8.dp)
                                    .height(8.dp)
                                    .background(colors.diagnosticError, shape = androidx.compose.foundation.shape.CircleShape)
                            )
                        }
                    }
                }
            }

            var lastClickTime by remember(state) { mutableLongStateOf(0L) }
            var clickCount by remember(state) { mutableStateOf(0) }
            var lastClickPos by remember(state) { mutableStateOf(CursorPosition.ZERO) }
            val multiClickTimeout = 400L

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .focusRequester(focusRequester)
                    .focusable()
                    .onFocusChanged { focusState ->
                        state.isFocused = focusState.isFocused
                    }
                    .onKeyEvent { event ->
                        KeyboardHandler.handleKeyEvent(event, state) {
                            onTextChange(state.document.getText())
                        }
                    }
                    .pointerInput(state) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Press -> {
                                        val currentTime = System.currentTimeMillis()
                                        val offset = event.changes.first().position
                                        val pos = calculatePosition(offset.x, offset.y)

                                        focusRequester.requestFocus()

                                        val samePosition = pos.line == lastClickPos.line
                                        if (currentTime - lastClickTime < multiClickTimeout && samePosition) {
                                            clickCount++
                                        } else {
                                            clickCount = 1
                                        }
                                        lastClickTime = currentTime
                                        lastClickPos = pos

                                        when (clickCount) {
                                            1 -> {
                                                state.moveCursor(pos)
                                                state.clearSelection()
                                            }
                                            2 -> {
                                                state.selectWord(pos)
                                            }
                                            3 -> {
                                                state.selectLine(pos.line)
                                                clickCount = 0
                                            }
                                        }

                                        event.changes.forEach { it.consume() }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                    .pointerInput(state) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                focusRequester.requestFocus()
                                val pos = calculatePosition(offset.x, offset.y)
                                state.updateSelection(SelectionRange(pos, pos))
                            },
                            onDrag = { change, _ ->
                                val pos = calculatePosition(change.position.x, change.position.y)
                                state.selection?.let { sel ->
                                    state.updateSelection(SelectionRange(sel.anchor, pos))
                                }
                                change.consume()
                            }
                        )
                    }
            ) {
                LazyColumn(
                    state = state.scrollState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = state.document.lines,
                        key = { index, _ -> index }
                    ) { lineIndex, lineContent ->
                        val highlightedText = remember(lineContent, diagnosticsByLine[lineIndex]) {
                            syntaxCache?.getHighlightedLine(
                                lineIndex = lineIndex,
                                lineContent = lineContent,
                                diagnosticsForLine = diagnosticsByLine[lineIndex] ?: emptyList()
                            ) ?: AnnotatedString(lineContent)
                        }

                        LineRenderer(
                            lineIndex = lineIndex,
                            lineContent = lineContent,
                            state = state,
                            highlightedText = highlightedText,
                            diagnostics = diagnosticsByLine[lineIndex] ?: emptyList(),
                            textStyle = textStyle,
                            onDiagnosticHover = { diagnostic, x, y ->
                                hoverJob?.cancel()
                                if (diagnostic != null) {
                                    hoverJob = coroutineScope.launch {
                                        delay(hoverDelayMs)
                                        hoverInfo = HoverInfo(diagnostic, x, y)
                                    }
                                } else {
                                    hoverJob = coroutineScope.launch {
                                        delay(100L)
                                        hoverInfo = null
                                    }
                                }
                            }
                        )
                    }
                }

                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(state.scrollState),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )
            }
        }

        LaunchedEffect(state) {
            focusRequester.requestFocus()
        }

        DiagnosticHoverPopup(
            hoverInfo = hoverInfo,
            onDismiss = {
                hoverJob?.cancel()
                hoverInfo = null
            },
            onApplyFix = { fix ->
                onApplyFix?.invoke(fix)
                hoverInfo = null
            }
        )
    }
}
