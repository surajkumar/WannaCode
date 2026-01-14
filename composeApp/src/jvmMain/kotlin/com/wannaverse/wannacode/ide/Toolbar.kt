package com.wannaverse.wannacode.ide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.common.Dropdown
import com.wannaverse.wannacode.common.PlainDropdown
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.ide.terminal.TerminalViewModel
import com.wannaverse.wannacode.theme.WannaCodeTheme
import com.wannaverse.wannacode.windowMovement
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.close_cross
import wannacode.composeapp.generated.resources.minimize
import wannacode.composeapp.generated.resources.play

@Composable
fun Toolbar(
    viewModel: CodeEditorViewModel,
    terminalViewModel: TerminalViewModel,
    window: ComposeWindow? = null
) {
    val colors = WannaCodeTheme.colors

    val runCommands = listOf(
        "./gradlew run",
        "./gradlew build",
        "./gradlew clean",
        "./gradlew test",
        "./gradlew assemble"
    )
    var selectedCommand by remember { mutableStateOf(runCommands[0]) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .border(1.dp, colors.border)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp
            )
            .then(
                if (window != null) {
                    Modifier.pointerInput(Unit) {
                        windowMovement(window)
                    }
                } else Modifier
            )
    ) {
        Row {
            Hamburger()

            Spacer(Modifier.width(30.dp))

            Dropdown(
                selectedOption = viewModel.projectName.value,
                options = listOf(),
                onOptionSelected = { },
                modifier = Modifier.width(200.dp).height(43.dp).padding(bottom = 10.dp).offset(y = (-5).dp),
                backgroundColor = colors.toolbarBackground,
                alignTextAlign = TextAlign.Center
            )

            Spacer(Modifier.width(20.dp))

            PlainDropdown(
                selectedOption = "Version Control",
                options = listOf("Pull", "Commit", "Push", "New Branch", "Change Branch"),
                onOptionSelected = {},
                modifier = Modifier.width(130.dp).offset(y = (-3).dp)
            )

            Spacer(Modifier.weight(1f))

            Dropdown(
                selectedOption = selectedCommand,
                options = runCommands,
                onOptionSelected = { selectedCommand = it },
                modifier = Modifier.width(200.dp).offset(y = (-10).dp).height(40.dp)
            )

            Spacer(Modifier.width(10.dp))

            val playInteractionSource = remember { MutableInteractionSource() }
            val isPlayHovered by playInteractionSource.collectIsHoveredAsState()

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .offset(y = (-5).dp)
                    .hoverable(playInteractionSource)
                    .background(
                        color = if (isPlayHovered) colors.success.copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable {
                        terminalViewModel.runCommandInNewSession(selectedCommand, "Run")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.play),
                    contentDescription = "Run",
                    tint = colors.toolbarIconSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(100.dp))

            Icon(
                painter = painterResource(Res.drawable.minimize),
                contentDescription = null,
                tint = colors.toolbarIcon,
                modifier = Modifier.padding(top = 1.dp).size(10.dp)
            )

            Spacer(Modifier.width(15.dp))

            Icon(
                painter = painterResource(Res.drawable.close_cross),
                contentDescription = null,
                tint = colors.toolbarIcon,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}
