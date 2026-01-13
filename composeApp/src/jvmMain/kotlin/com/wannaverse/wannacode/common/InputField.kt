package com.wannaverse.wannacode.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.theme.WannaCodeTheme

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hintText: @Composable () -> Unit = {},
    icon: @Composable () -> Unit = {},
    isError: Boolean = false
) {
    val colors = WannaCodeTheme.colors
    val strokeColor = if (isError) colors.error else colors.inputBorder

    Column(modifier = modifier.padding(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(colors.inputBackground, shape = RoundedCornerShape(5.dp))
                .border(1.dp, strokeColor, shape = RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = colors.inputText,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(colors.inputText),
                    modifier = Modifier.weight(1f)
                )

                icon()
            }
        }

        hintText()
    }
}
