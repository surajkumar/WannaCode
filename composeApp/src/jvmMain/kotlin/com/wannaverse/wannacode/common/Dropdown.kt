package com.wannaverse.wannacode.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.theme.WannaCodeTheme
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.down_cheveron

@Composable
fun Dropdown(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    alignTextAlign: TextAlign = TextAlign.Start
) {
    val colors = WannaCodeTheme.colors
    val bgColor = backgroundColor ?: colors.dropdownBackground
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, shape = RoundedCornerShape(5.dp))
            .border(1.dp, colors.dropdownBorder, shape = RoundedCornerShape(5.dp))
            .height(40.dp)
            .clickable { expanded = !expanded },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedOption,
                color = colors.dropdownText,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f).offset(x = (-5).dp),
                textAlign = alignTextAlign
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
                .background(colors.dropdownBackground)
                .border(1.dp, colors.dropdownBorder, RoundedCornerShape(5.dp))
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
                            color = colors.dropdownText,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }
    }
}
