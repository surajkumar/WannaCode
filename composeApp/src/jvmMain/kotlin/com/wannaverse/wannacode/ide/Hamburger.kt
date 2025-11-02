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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.hamburg

@Composable
fun Hamburger() {
    var expanded by remember { mutableStateOf(false) }

    Icon(
        painter = painterResource(Res.drawable.hamburg),
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(15.dp).clickable { expanded = !expanded }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier
            .background(Color(0xFF17171D))
            .border(1.dp, Color(0xFF373737), RoundedCornerShape(5.dp))
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = "New Project",
                    color = Color(0xFFB6B6B6),
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
                    color = Color(0xFFB6B6B6),
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
                    color = Color(0xFFB6B6B6),
                    fontSize = 14.sp
                )
            },
            onClick = {
                expanded = false
            }
        )
    }
}
