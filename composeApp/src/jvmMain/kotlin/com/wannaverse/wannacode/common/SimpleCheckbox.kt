package com.wannaverse.wannacode.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.theme.WannaCodeTheme
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.checkmark

@Composable
fun SimpleCheckbox(
    checked: Boolean,
    onCheckChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WannaCodeTheme.colors

    Box(
        modifier = modifier
            .size(25.dp)
            .background(
                color = colors.checkboxBackground,
                shape = RoundedCornerShape(5.dp)
            )
            .border(
                width = 1.dp,
                color = colors.checkboxBorder,
                shape = RoundedCornerShape(5.dp)
            )
            .clickable { onCheckChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                painter = painterResource(Res.drawable.checkmark),
                contentDescription = "Checked",
                tint = colors.checkboxCheck,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
