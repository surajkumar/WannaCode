package com.wannaverse.wannacode.ide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.theme.WannaCodeTheme
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.hamburg

@Composable
fun Hamburger() {
    val colors = WannaCodeTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Icon(
        painter = painterResource(Res.drawable.hamburg),
        contentDescription = null,
        tint = colors.toolbarIcon,
        modifier = Modifier.size(15.dp).clickable { expanded = !expanded }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier
            .background(colors.menuBackground)
            .border(1.dp, colors.menuBorder, RoundedCornerShape(5.dp))
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = "New Project",
                    color = colors.menuText,
                    fontSize = 14.sp
                )
            },
            onClick = {
                expanded = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Open",
                    color = colors.menuText,
                    fontSize = 14.sp
                )
            },
            onClick = {
                expanded = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Close",
                    color = colors.menuText,
                    fontSize = 14.sp
                )
            },
            onClick = {
                expanded = false
            }
        )
    }
}
