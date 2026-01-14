package com.wannaverse.wannacode.ide.terminal

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.theme.WannaCodeTheme
import java.awt.Cursor
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.close_cross

@Composable
fun Terminal(
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    val colors = WannaCodeTheme.colors
    var panelHeight by remember { mutableStateOf(200.dp) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(colors.border)
                .pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panelHeight = (panelHeight - dragAmount.y.dp).coerceIn(100.dp, 500.dp)
                    }
                }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(panelHeight)
                .background(colors.backgroundSecondary)
        ) {
            TerminalTabBar(viewModel)

            if (viewModel.currentSessionId != -1) {
                TerminalContent(
                    viewModel = viewModel,
                    sessionId = viewModel.currentSessionId,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No terminal sessions",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Click + to create a new terminal",
                            color = colors.textTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalTabBar(viewModel: TerminalViewModel) {
    val colors = WannaCodeTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(colors.backgroundTertiary)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TERMINAL",
            color = colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(end = 16.dp)
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start
        ) {
            viewModel.orderedSessionIds.forEach { sessionId ->
                val session = viewModel.sessions[sessionId] ?: return@forEach
                val isActive = sessionId == viewModel.currentSessionId

                TerminalTab(
                    name = session.name,
                    isActive = isActive,
                    isRunning = viewModel.isSessionRunning(sessionId),
                    onClick = { viewModel.switchSession(sessionId) },
                    onClose = { viewModel.closeSession(sessionId) }
                )

                Spacer(Modifier.width(2.dp))
            }
        }

        val addInteractionSource = remember { MutableInteractionSource() }
        val isAddHovered by addInteractionSource.collectIsHoveredAsState()

        Box(
            modifier = Modifier
                .size(24.dp)
                .hoverable(addInteractionSource)
                .background(
                    color = if (isAddHovered) colors.surfaceHover else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { viewModel.createSession() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = colors.textSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
private fun TerminalTab(
    name: String,
    isActive: Boolean,
    isRunning: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val colors = WannaCodeTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .height(28.dp)
            .hoverable(interactionSource)
            .background(
                color = when {
                    isActive -> colors.backgroundSecondary
                    isHovered -> colors.surfaceHover
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "●",
            color = if (isRunning) colors.success else colors.textTertiary,
            fontSize = 6.sp,
            modifier = Modifier.padding(end = 6.dp)
        )

        Text(
            text = name,
            color = if (isActive) colors.textPrimary else colors.textSecondary,
            fontSize = 12.sp,
            maxLines = 1
        )

        if (isHovered || isActive) {
            Spacer(Modifier.width(8.dp))

            val closeInteraction = remember { MutableInteractionSource() }
            val isCloseHovered by closeInteraction.collectIsHoveredAsState()

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .hoverable(closeInteraction)
                    .background(
                        color = if (isCloseHovered) colors.error.copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.close_cross),
                    contentDescription = "Close",
                    tint = if (isCloseHovered) colors.error else colors.textTertiary,
                    modifier = Modifier.size(8.dp)
                )
            }
        }
    }
}

@Composable
private fun TerminalContent(
    viewModel: TerminalViewModel,
    sessionId: Int,
    modifier: Modifier = Modifier
) {
    val lines = viewModel.getSessionLines(sessionId)
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current

    var hiddenInput by remember { mutableStateOf(TextFieldValue("")) }

    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(530)
            cursorVisible = !cursorVisible
        }
    }

    val (cursorRow, cursorCol) = viewModel.getCursorPosition(sessionId)

    LaunchedEffect(cursorRow, lines.size) {
        if (cursorRow >= 0 && cursorRow < lines.size) {
            listState.animateScrollToItem(cursorRow)
        } else if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .onSizeChanged { size ->
                with(density) {
                    val charWidth = 7.8f
                    val lineHeight = 18.sp.toPx()
                    val cols = ((size.width - 24.dp.toPx()) / charWidth).toInt().coerceAtLeast(40)
                    val rows = (size.height / lineHeight).toInt().coerceAtLeast(10)
                    viewModel.resizeTerminal(sessionId, cols, rows)
                }
            }
    ) {
        BasicTextField(
            value = hiddenInput,
            onValueChange = { newValue ->
                val newText = newValue.text
                val oldText = hiddenInput.text
                if (newText.length > oldText.length) {
                    val added = newText.substring(oldText.length)
                    viewModel.sendInput(sessionId, added)
                }
                hiddenInput = TextFieldValue("")
            },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                    val isCtrl = event.isCtrlPressed
                    val isAlt = event.isAltPressed

                    if (isCtrl && !isAlt) {
                        val char = when (event.key) {
                            Key.A -> 'a'
                            Key.B -> 'b'
                            Key.C -> 'c'
                            Key.D -> 'd'
                            Key.E -> 'e'
                            Key.F -> 'f'
                            Key.G -> 'g'
                            Key.H -> 'h'
                            Key.I -> 'i'
                            Key.J -> 'j'
                            Key.K -> 'k'
                            Key.L -> 'l'
                            Key.M -> 'm'
                            Key.N -> 'n'
                            Key.O -> 'o'
                            Key.P -> 'p'
                            Key.Q -> 'q'
                            Key.R -> 'r'
                            Key.S -> 's'
                            Key.T -> 't'
                            Key.U -> 'u'
                            Key.V -> 'v'
                            Key.W -> 'w'
                            Key.X -> 'x'
                            Key.Y -> 'y'
                            Key.Z -> 'z'
                            else -> null
                        }
                        if (char != null) {
                            viewModel.sendControlKey(sessionId, char)
                            return@onPreviewKeyEvent true
                        }
                    }

                    val specialKey = when (event.key) {
                        Key.Enter, Key.NumPadEnter -> TerminalKey.ENTER
                        Key.Backspace -> TerminalKey.BACKSPACE
                        Key.Tab -> TerminalKey.TAB
                        Key.Escape -> TerminalKey.ESCAPE
                        Key.DirectionUp -> TerminalKey.UP
                        Key.DirectionDown -> TerminalKey.DOWN
                        Key.DirectionLeft -> TerminalKey.LEFT
                        Key.DirectionRight -> TerminalKey.RIGHT
                        Key.Home, Key.MoveHome -> TerminalKey.HOME
                        Key.MoveEnd -> TerminalKey.END
                        Key.PageUp -> TerminalKey.PAGE_UP
                        Key.PageDown -> TerminalKey.PAGE_DOWN
                        Key.Delete -> TerminalKey.DELETE
                        Key.Insert -> TerminalKey.INSERT
                        Key.F1 -> TerminalKey.F1
                        Key.F2 -> TerminalKey.F2
                        Key.F3 -> TerminalKey.F3
                        Key.F4 -> TerminalKey.F4
                        Key.F5 -> TerminalKey.F5
                        Key.F6 -> TerminalKey.F6
                        Key.F7 -> TerminalKey.F7
                        Key.F8 -> TerminalKey.F8
                        Key.F9 -> TerminalKey.F9
                        Key.F10 -> TerminalKey.F10
                        Key.F11 -> TerminalKey.F11
                        Key.F12 -> TerminalKey.F12
                        else -> null
                    }

                    if (specialKey != null) {
                        viewModel.sendKey(sessionId, specialKey)
                        return@onPreviewKeyEvent true
                    }

                    false
                },
            textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                }
        ) {
            SelectionContainer {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    itemsIndexed(
                        items = lines,
                        key = { index, _ -> index }
                    ) { lineIndex, line ->
                        TerminalLineView(
                            line = line,
                            lineIndex = lineIndex,
                            cursorRow = cursorRow,
                            cursorCol = cursorCol,
                            cursorVisible = cursorVisible
                        )
                    }
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = 2.dp),
                style = LocalScrollbarStyle.current.copy(
                    unhoverColor = Color.White.copy(alpha = 0.2f),
                    hoverColor = Color.White.copy(alpha = 0.4f),
                    minimalHeight = 24.dp
                )
            )
        }
    }

    LaunchedEffect(sessionId) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun TerminalLineView(
    line: TerminalLine,
    lineIndex: Int,
    cursorRow: Int,
    cursorCol: Int,
    cursorVisible: Boolean
) {
    val showCursor = lineIndex == cursorRow && cursorVisible

    val annotatedString = buildAnnotatedString {
        if (line.chars.isEmpty()) {
            if (showCursor && cursorCol == 0) {
                withStyle(SpanStyle(background = Color(0xFFCCCCCC))) {
                    append(" ")
                }
            } else {
                append(" ")
            }
        } else {
            line.chars.forEachIndexed { index, termChar ->
                val isCursorHere = showCursor && index == cursorCol

                withStyle(
                    SpanStyle(
                        color = termChar.foreground,
                        background = if (isCursorHere) Color(0xFFCCCCCC) else termChar.background,
                        fontWeight = if (termChar.bold) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                ) {
                    append(if (isCursorHere && termChar.char == ' ') "█" else termChar.char.toString())
                }
            }

            if (showCursor && cursorCol >= line.chars.size) {
                withStyle(SpanStyle(background = Color(0xFFCCCCCC))) {
                    append(" ")
                }
            }
        }
    }

    Text(
        text = annotatedString,
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 18.sp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    )
}
