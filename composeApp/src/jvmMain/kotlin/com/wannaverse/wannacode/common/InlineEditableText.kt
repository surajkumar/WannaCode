package com.wannaverse.wannacode.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.theme.WannaCodeTheme

private val INVALID_FILENAME_CHARS = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

private fun filterInvalidChars(input: String): String = input.filterNot { it in INVALID_FILENAME_CHARS }

@Composable
fun InlineEditableText(
    text: String,
    isEditing: Boolean,
    onEditComplete: (String) -> Unit,
    onEditCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WannaCodeTheme.colors

    if (isEditing) {
        val focusRequester = remember { FocusRequester() }
        var textFieldValue by remember(text) {
            mutableStateOf(
                TextFieldValue(
                    text = text,
                    selection = TextRange(0, text.lastIndexOf('.').takeIf { it > 0 } ?: text.length)
                )
            )
        }
        var hasFocus by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Box(
            modifier = modifier
                .widthIn(min = 80.dp, max = 200.dp)
                .background(colors.inputBackground, shape = RoundedCornerShape(3.dp))
                .border(1.dp, colors.accent, shape = RoundedCornerShape(3.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    val filtered = filterInvalidChars(newValue.text)
                    textFieldValue = if (filtered != newValue.text) {
                        newValue.copy(
                            text = filtered,
                            selection = TextRange(filtered.length.coerceAtMost(newValue.selection.start))
                        )
                    } else {
                        newValue
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = colors.inputText,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { hasFocus = it.isFocused }
                    .onKeyEvent { event ->
                        when (event.key) {
                            Key.Enter -> {
                                if (textFieldValue.text.isNotBlank()) {
                                    onEditComplete(textFieldValue.text)
                                }
                                true
                            }
                            Key.Escape -> {
                                onEditCancel()
                                true
                            }
                            else -> false
                        }
                    }
            )
        }
    } else {
        Text(
            text = text,
            color = colors.explorerText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    }
}
