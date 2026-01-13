package com.wannaverse.wannacode.splash.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wannaverse.wannacode.common.Dropdown
import com.wannaverse.wannacode.common.FolderSelector
import com.wannaverse.wannacode.common.InputField
import com.wannaverse.wannacode.common.SimpleCheckbox
import com.wannaverse.wannacode.splash.BuildSystemInstallation
import com.wannaverse.wannacode.splash.SplashPageViewModel
import com.wannaverse.wannacode.theme.WannaCodeTheme
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.folder

@Composable
fun MavenSettings(viewModel: SplashPageViewModel) {
    val colors = WannaCodeTheme.colors

    LaunchedEffect(Unit) {
        viewModel.fetchMavenVersions()
    }

    var showFolderDialog by remember { mutableStateOf(false) }
    if (showFolderDialog) {
        FolderSelector(
            onFolderSelected = {
                viewModel.buildToolInstallationPath.value = it.absolutePath
                showFolderDialog = false
            },
            onCancel = { showFolderDialog = false }
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(text = "Wrapper", color = colors.textPrimary)

        SimpleCheckbox(
            checked = viewModel.selectedBuildSystemInstallation.value == BuildSystemInstallation.WRAPPER,
            onCheckChange = {
                viewModel.selectedBuildSystemInstallation.value = BuildSystemInstallation.WRAPPER
            }
        )

        Text(text = "Local Installation", color = colors.textPrimary)

        SimpleCheckbox(
            checked = viewModel.selectedBuildSystemInstallation.value == BuildSystemInstallation.LOCAL,
            onCheckChange = {
                viewModel.selectedBuildSystemInstallation.value = BuildSystemInstallation.LOCAL
            }
        )
    }

    Spacer(Modifier.height(10.dp))

    if (viewModel.selectedBuildSystemInstallation.value == BuildSystemInstallation.LOCAL) {
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            Text(
                text = "Maven Installation",
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 10.dp)
            )

            InputField(
                value = viewModel.buildToolInstallationPath.value,
                onValueChange = {
                    viewModel.buildToolInstallationPath.value = it
                },
                hintText = {
                },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.folder),
                        contentDescription = "Browse folder",
                        tint = colors.textTertiary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showFolderDialog = true }
                    )
                }
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Distribution",
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 5.dp)
        )

        Dropdown(
            selectedOption = viewModel.selectedMavenDistribution.value,
            options = viewModel.mavenDistributions.value,
            onOptionSelected = { viewModel.selectedMavenDistribution.value = it }
        )
    }

    Spacer(Modifier.height(1.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Packaging",
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 5.dp)
        )

        Dropdown(
            selectedOption = viewModel.selectedMavenPackaging.value,
            options = viewModel.mavenPackagingOptions,
            onOptionSelected = { viewModel.selectedMavenPackaging.value = it }
        )
    }

    Spacer(Modifier.height(1.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(23.dp)) {
        Text(
            text = "Group ID",
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 10.dp)
        )

        InputField(
            value = viewModel.groupId.value,
            onValueChange = {
                viewModel.groupId.value = it
            },
            hintText = {
                viewModel.getGroupIdError()?.let {
                    Text(
                        text = it,
                        color = colors.error,
                        fontSize = 12.sp
                    )
                }
            },
            isError = viewModel.getGroupIdError() != null
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Artifact ID",
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 10.dp)
        )

        InputField(
            value = viewModel.artifactId.value,
            onValueChange = {
                viewModel.artifactId.value = it
            },
            hintText = {
                viewModel.getArtifactIdError()?.let {
                    Text(
                        text = it,
                        color = colors.error,
                        fontSize = 12.sp
                    )
                }
            },
            isError = viewModel.getArtifactIdError() != null
        )
    }
}
