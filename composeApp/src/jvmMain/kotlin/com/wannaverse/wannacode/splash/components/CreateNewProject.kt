package com.wannaverse.wannacode.splash.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.ERROR_RED
import com.wannaverse.wannacode.SECONDARY_TEXT_COLOR
import com.wannaverse.wannacode.common.Dropdown
import com.wannaverse.wannacode.common.FolderSelector
import com.wannaverse.wannacode.common.InputField
import com.wannaverse.wannacode.common.PrimaryButton
import com.wannaverse.wannacode.common.SecondaryButton
import com.wannaverse.wannacode.common.SimpleCheckbox
import com.wannaverse.wannacode.splash.BuildSystem
import com.wannaverse.wannacode.splash.SplashPageViewModel
import java.util.Locale.getDefault
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.folder

@Composable
fun CreateNewProject(viewModel: SplashPageViewModel, hideSplash: () -> Unit) {
    var showFolderDialog by remember { mutableStateOf(false) }

    if (showFolderDialog) {
        FolderSelector { folder ->
            viewModel.location.value = folder.absolutePath
            showFolderDialog = false
        }
    }

    var isError by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Create New Project",
            color = SECONDARY_TEXT_COLOR,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(150.dp)) {
                Text(
                    text = "Name",
                    modifier = Modifier.padding(top = 10.dp)
                )

                InputField(
                    value = viewModel.projectName.value,
                    onValueChange = { viewModel.projectName.value = it },
                    hintText = {
                        viewModel.getProjectNameError()?.let {
                            Text(
                                text = it,
                                color = ERROR_RED,
                                fontSize = 12.sp
                            )
                        }
                    },
                    isError = !viewModel.isProjectNameValid()
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(130.dp)) {
                Text(
                    text = "Location",
                    modifier = Modifier.padding(top = 10.dp)
                )

                InputField(
                    value = viewModel.location.value,
                    onValueChange = { viewModel.location.value = it },
                    hintText = {
                        if (viewModel.getLocationError() == null) {
                            Text(
                                text = "Project will be created in this directory",
                                color = Color(0xFF888888),
                                fontSize = 12.sp
                            )
                        }

                        viewModel.getLocationError()?.let {
                            Text(
                                text = it,
                                color = ERROR_RED,
                                fontSize = 12.sp
                            )
                        }
                    },
                    isError = viewModel.getLocationError() != null,
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.folder),
                            contentDescription = "Browse folder",
                            tint = Color(0xFFB0B0B0),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    showFolderDialog = true
                                }
                        )
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                Text(
                    text = "Create Git Repository"
                )

                SimpleCheckbox(
                    checked = viewModel.shouldCreateGitRepository.value,
                    onCheckChange = {
                        if (viewModel.checkGit()) {
                            viewModel.shouldCreateGitRepository.value = it
                        } else {
                            viewModel.shouldCreateGitRepository.value = false
                        }
                    }
                )

                viewModel.getGitError()?.let {
                    Text(
                        text = it,
                        color = ERROR_RED,
                        fontSize = 12.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(105.dp)) {
                Text(text = "Java Version")

                Dropdown(
                    selectedOption = viewModel.selectedJavaVersion.value,
                    options = viewModel.javaOptions,
                    onOptionSelected = { viewModel.selectedJavaVersion.value = it },
                    modifier = Modifier.width(60.dp),
                    alignTextAlign = TextAlign.Center
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(100.dp)) {
                Text(text = "Build System")

                Dropdown(
                    selectedOption = viewModel.selectedBuildSystem.value.name.lowercase(getDefault()).replaceFirstChar { it.titlecase() },
                    options = viewModel.buildSystemOptions,
                    onOptionSelected = { viewModel.selectedBuildSystem.value = BuildSystem.valueOf(it.uppercase()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.padding(start = 200.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (viewModel.selectedBuildSystem.value == BuildSystem.GRADLE) {
                GradleSettings(viewModel)
            } else if (viewModel.selectedBuildSystem.value == BuildSystem.MAVEN) {
                MavenSettings(viewModel)
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            PrimaryButton(
                text = "Create Project",
                onClick = {
                    viewModel.createProject(hideSplash)
                    isError = viewModel.isError.value
                },
                enabled = !viewModel.hasErrors()
            )
            SecondaryButton(
                text = "Reset Form",
                onClick = {
                    viewModel.reset()
                }
            )
        }

        if (isError) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = viewModel.errorMessage.value,
                color = ERROR_RED,
                fontSize = 12.sp
            )
        }
    }
}
