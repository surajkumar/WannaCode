package com.wannaverse.wannacode.ide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.hamburg
import wannacode.composeapp.generated.resources.right_cheveron

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Hamburger() {
    var expanded by remember { mutableStateOf(false) }
    var subMenuExpanded by remember { mutableStateOf(false) }

    Box {
        Icon(
            painter = painterResource(Res.drawable.hamburg),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(15.dp)
                .clickable { expanded = !expanded }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                subMenuExpanded = false
            },
            modifier = Modifier
                .background(Color(0xFF17171D))
                .border(1.dp, Color(0xFF373737), RoundedCornerShape(5.dp))
        ) {
            DropdownMenuItem(
                text = {
                    Row {
                        Text("View", color = Color(0xFFB6B6B6), fontSize = 14.sp)
                        Spacer(Modifier.width(50.dp))
                        Icon(
                            painter = painterResource(Res.drawable.right_cheveron),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.padding(top = 5.dp).size(10.dp)
                        )
                    }
                },
                onClick = {},
                modifier = Modifier.pointerMoveFilter(
                    onEnter = {
                        subMenuExpanded = true
                        false
                    },
                    onExit = {
                        false
                    }
                )
            )

            DropdownMenuItem(
                text = { Text("New Project", color = Color(0xFFB6B6B6), fontSize = 14.sp) },
                onClick = { expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Open", color = Color(0xFFB6B6B6), fontSize = 14.sp) },
                onClick = { expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Close", color = Color(0xFFB6B6B6), fontSize = 14.sp) },
                onClick = { expanded = false }
            )
        }

        if (subMenuExpanded) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { subMenuExpanded = false },
                offset = DpOffset(x = 110.dp, y = 0.dp),
                modifier = Modifier
                    .background(Color(0xFF17171D))
                    .border(1.dp, Color(0xFF373737), RoundedCornerShape(5.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Line Encodings", color = Color(0xFFB6B6B6), fontSize = 14.sp) },
                    onClick = {
                        subMenuExpanded = false
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Whitespace", color = Color(0xFFB6B6B6), fontSize = 14.sp) },
                    onClick = {
                        subMenuExpanded = false
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Special Characters", color = Color(0xFFB6B6B6), fontSize = 14.sp) },
                    onClick = {
                        subMenuExpanded = false
                        expanded = false
                    }
                )
            }
        }
    }
}

