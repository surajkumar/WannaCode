package com.wannaverse.wannacode.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.theme.WannaCodeTheme

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit
) {
    val colors = WannaCodeTheme.colors

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colors.buttonSecondary,
            contentColor = colors.buttonSecondaryText
        ),
        contentPadding = PaddingValues(15.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
