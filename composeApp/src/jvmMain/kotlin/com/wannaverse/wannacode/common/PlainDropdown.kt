package com.wannaverse.wannacode.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.theme.WannaCodeTheme
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.down_cheveron

@Composable
fun PlainDropdown(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WannaCodeTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clickable { expanded = !expanded },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedOption,
                color = colors.textTertiary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(Res.drawable.down_cheveron),
                contentDescription = null,
                tint = colors.textSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(colors.menuBackground)
                .border(1.dp, colors.menuBorder, RoundedCornerShape(5.dp))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    text = {
                        Text(
                            text = option,
                            color = colors.menuText,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }
    }
}
