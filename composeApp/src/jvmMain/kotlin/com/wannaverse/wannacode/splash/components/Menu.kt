package com.wannaverse.wannacode.splash.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.ACTIVE_TEXT_COLOR
import com.wannaverse.wannacode.PRIMARY_TEXT_COLOR
import com.wannaverse.wannacode.STROKE_COLOR
import com.wannaverse.wannacode.common.FolderSelector
import com.wannaverse.wannacode.splash.SplashPageOption
import com.wannaverse.wannacode.splash.SplashPageViewModel

@Composable
fun Menu(viewModel: SplashPageViewModel, hideSplash: () -> Unit) {
    var showOpenProject by remember { mutableStateOf(false) }

    if (showOpenProject) {
        FolderSelector(
            onFolderSelected = { folder ->
                viewModel.location.value = folder.parent
                viewModel.projectName.value = folder.name
                viewModel.showIDE.value = true
                showOpenProject = false
                hideSplash()
            },
            onCancel = { showOpenProject = false }
        )
    }

    Column {
        Row(
            modifier = Modifier.padding(start = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val x = size.width - strokeWidth / 2
                        drawLine(
                            color = STROKE_COLOR,
                            start = Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
                    .padding(20.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Recent",
                    color = if (viewModel.activeScreen == SplashPageOption.RECENT) ACTIVE_TEXT_COLOR else PRIMARY_TEXT_COLOR,
                    modifier = Modifier.clickable(onClick = {
                        viewModel.activeScreen = SplashPageOption.RECENT
                    })
                )

                Text(
                    text = "Create New Project",
                    color = if (viewModel.activeScreen == SplashPageOption.CREATE) ACTIVE_TEXT_COLOR else PRIMARY_TEXT_COLOR,
                    modifier = Modifier.clickable(onClick = {
                        viewModel.activeScreen = SplashPageOption.CREATE
                    })
                )

                Text(
                    text = "Open",
                    color = if (viewModel.activeScreen == SplashPageOption.OPEN) ACTIVE_TEXT_COLOR else PRIMARY_TEXT_COLOR,
                    modifier = Modifier.clickable(onClick = {
                        showOpenProject = true
                    })
                )

                Text(
                    text = "Clone Repository",
                    color = if (viewModel.activeScreen == SplashPageOption.CLONE_REPOSITORY) ACTIVE_TEXT_COLOR else PRIMARY_TEXT_COLOR,
                    modifier = Modifier.clickable(onClick = {
                        viewModel.activeScreen = SplashPageOption.CLONE_REPOSITORY
                    })
                )

                Text(
                    text = "Settings",
                    color = if (viewModel.activeScreen == SplashPageOption.SETTINGS) ACTIVE_TEXT_COLOR else PRIMARY_TEXT_COLOR,
                    modifier = Modifier.clickable(onClick = {
                        viewModel.activeScreen = SplashPageOption.SETTINGS
                    })
                )

                Text(
                    text = "Exit",
                    color = PRIMARY_TEXT_COLOR,
                    modifier = Modifier.clickable(onClick = {
                        viewModel.closeProgram()
                    })
                )
            }
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                when (viewModel.activeScreen) {
                    SplashPageOption.RECENT -> {}
                    SplashPageOption.CREATE -> CreateNewProject(viewModel, hideSplash)
                    SplashPageOption.OPEN -> {}
                    SplashPageOption.CLONE_REPOSITORY -> {}
                    SplashPageOption.SETTINGS -> {}
                }
            }
        }
    }
}
