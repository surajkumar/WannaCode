package com.wannaverse.wannacode.ide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.common.Dropdown
import com.wannaverse.wannacode.common.PlainDropdown
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.ide.runconfig.RunConfiguration
import com.wannaverse.wannacode.ide.runconfig.RunConfigurationDialog
import com.wannaverse.wannacode.ide.runconfig.RunConfigurationViewModel
import com.wannaverse.wannacode.ide.terminal.TerminalViewModel
import com.wannaverse.wannacode.theme.WannaCodeTheme
import com.wannaverse.wannacode.windowMovement
import java.io.File
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.close_cross
import wannacode.composeapp.generated.resources.down_cheveron
import wannacode.composeapp.generated.resources.minimize
import wannacode.composeapp.generated.resources.play

@Composable
fun Toolbar(
    viewModel: CodeEditorViewModel,
    terminalViewModel: TerminalViewModel,
    runConfigViewModel: RunConfigurationViewModel,
    window: ComposeWindow? = null
) {
    val colors = WannaCodeTheme.colors

    var showConfigDialog by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<RunConfiguration?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

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

            RunConfigurationDropdown(
                configurations = runConfigViewModel.configurations,
                selectedConfiguration = runConfigViewModel.selectedConfiguration,
                onSelect = { runConfigViewModel.selectConfiguration(it) },
                onEditConfiguration = { config ->
                    editingConfig = config
                    isCreatingNew = false
                    showConfigDialog = true
                },
                onAddNew = {
                    editingConfig = null
                    isCreatingNew = true
                    showConfigDialog = true
                },
                modifier = Modifier.width(200.dp).offset(y = (-10).dp).height(40.dp)
            )

            Spacer(Modifier.width(10.dp))

            val playInteractionSource = remember { MutableInteractionSource() }
            val isPlayHovered by playInteractionSource.collectIsHoveredAsState()
            val selectedConfig = runConfigViewModel.selectedConfiguration

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .offset(y = (-5).dp)
                    .hoverable(playInteractionSource)
                    .background(
                        color = if (isPlayHovered && selectedConfig != null) colors.success.copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable(enabled = selectedConfig != null) {
                        selectedConfig?.let { config ->
                            val workDir = config.workingDirectory.takeIf { it.isNotBlank() }?.let { File(it) }
                            terminalViewModel.runCommandInNewSession(
                                command = config.command,
                                name = config.name,
                                environmentVariables = config.environmentVariables,
                                customWorkingDir = workDir
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.play),
                    contentDescription = "Run",
                    tint = if (selectedConfig != null) colors.toolbarIconSuccess else colors.textTertiary,
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

    if (showConfigDialog) {
        RunConfigurationDialog(
            configuration = if (isCreatingNew) null else editingConfig,
            onSave = { config ->
                if (isCreatingNew) {
                    runConfigViewModel.addConfiguration(config)
                } else {
                    runConfigViewModel.updateConfiguration(config)
                }
                showConfigDialog = false
                editingConfig = null
            },
            onDelete = if (!isCreatingNew && editingConfig != null) {
                {
                    editingConfig?.let { runConfigViewModel.deleteConfiguration(it) }
                    showConfigDialog = false
                    editingConfig = null
                }
            } else null,
            onDismiss = {
                showConfigDialog = false
                editingConfig = null
            }
        )
    }
}

@Composable
private fun RunConfigurationDropdown(
    configurations: List<RunConfiguration>,
    selectedConfiguration: RunConfiguration?,
    onSelect: (RunConfiguration) -> Unit,
    onEditConfiguration: (RunConfiguration) -> Unit,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WannaCodeTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.dropdownBackground, shape = RoundedCornerShape(5.dp))
            .border(1.dp, colors.dropdownBorder, shape = RoundedCornerShape(5.dp))
            .height(40.dp)
            .clickable { expanded = !expanded },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedConfiguration?.name ?: "No configuration",
                color = colors.dropdownText,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f).offset(x = (-5).dp),
                textAlign = TextAlign.Start
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
            configurations.forEach { config ->
                DropdownMenuItem(
                    onClick = {
                        onSelect(config)
                        expanded = false
                    },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = config.name,
                                color = colors.dropdownText,
                                fontSize = 14.sp
                            )

                            val editInteraction = remember { MutableInteractionSource() }
                            val isEditHovered by editInteraction.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .hoverable(editInteraction)
                                    .background(
                                        color = if (isEditHovered) colors.surfaceHover else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        expanded = false
                                        onEditConfiguration(config)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✎",
                                    color = if (isEditHovered) colors.textPrimary else colors.textTertiary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                )
            }

            if (configurations.isNotEmpty()) {
                Divider(
                    color = colors.border,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            DropdownMenuItem(
                onClick = {
                    expanded = false
                    onAddNew()
                },
                text = {
                    Text(
                        text = "+ Add Configuration",
                        color = colors.accent,
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}
