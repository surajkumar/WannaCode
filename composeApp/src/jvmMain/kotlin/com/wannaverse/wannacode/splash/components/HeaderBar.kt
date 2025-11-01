package com.wannaverse.wannacode.splash.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.PRIMARY_TEXT_COLOR
import com.wannaverse.wannacode.SECONDARY_TEXT_COLOR
import com.wannaverse.wannacode.STROKE_COLOR

@Composable
fun HeaderBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, STROKE_COLOR)
            .padding(20.dp)
    ) {
        Text(
            "WannaCode IDE",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = PRIMARY_TEXT_COLOR
        )

        Text(
            "2025.1.1",
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = SECONDARY_TEXT_COLOR
        )
    }
}
