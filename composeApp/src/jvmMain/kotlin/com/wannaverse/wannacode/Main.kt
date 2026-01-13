package com.wannaverse.wannacode

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wannaverse.wannacode.ide.IDEScreen
import com.wannaverse.wannacode.ide.editor.jdt.launchJdtServer
import com.wannaverse.wannacode.splash.SplashPage
import com.wannaverse.wannacode.splash.SplashPageViewModel
import com.wannaverse.wannacode.theme.WannaCodeTheme
import java.awt.GraphicsEnvironment
import java.awt.MouseInfo
import java.awt.Point

fun main() = application {
    var showSplash by remember { mutableStateOf(true) }
    var isDarkTheme by remember { mutableStateOf(true) }

    WannaCodeTheme(darkTheme = isDarkTheme) {
        val themeColors = WannaCodeTheme.colors

        val colorScheme = if (isDarkTheme) {
            darkColorScheme(
                primary = themeColors.accent,
                onPrimary = themeColors.textPrimary,
                primaryContainer = themeColors.background,
                onPrimaryContainer = themeColors.textPrimary,
                secondary = themeColors.textPrimary,
                onSecondary = themeColors.background,
                secondaryContainer = themeColors.backgroundSecondary,
                onSecondaryContainer = themeColors.textPrimary,
                background = themeColors.background,
                onBackground = themeColors.textPrimary,
                surface = themeColors.surface,
                onSurface = themeColors.textPrimary,
                onSurfaceVariant = themeColors.textSecondary,
                surfaceContainer = themeColors.backgroundSecondary,
                outline = themeColors.border,
                error = themeColors.error
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary = themeColors.accent,
                onPrimary = themeColors.textPrimary,
                primaryContainer = themeColors.background,
                onPrimaryContainer = themeColors.textPrimary,
                secondary = themeColors.textPrimary,
                onSecondary = themeColors.background,
                secondaryContainer = themeColors.backgroundSecondary,
                onSecondaryContainer = themeColors.textPrimary,
                background = themeColors.background,
                onBackground = themeColors.textPrimary,
                surface = themeColors.surface,
                onSurface = themeColors.textPrimary,
                onSurfaceVariant = themeColors.textSecondary,
                surfaceContainer = themeColors.backgroundSecondary,
                outline = themeColors.border,
                error = themeColors.error
            )
        }

        MaterialTheme(
            colorScheme = colorScheme
        ) {
            val viewModel = remember { SplashPageViewModel() }

            if (showSplash) {
                Window(
                    onCloseRequest = ::exitApplication,
                    title = "WannaCode",
                    undecorated = true,
                    state = rememberWindowState(
                        width = 1200.dp,
                        height = 1000.dp
                    )
                ) {
                    val window = this.window

                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    windowMovement(window)
                                }
                        ) {
                            SplashPage(
                                viewModel = viewModel { viewModel },
                                hideSplash = {
                                    showSplash = false
                                }
                            )
                        }
                    }
                }
            } else {
                val screenBounds = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .maximumWindowBounds

                val windowState = rememberWindowState(
                    position = WindowPosition(screenBounds.x.dp, screenBounds.y.dp),
                    width = screenBounds.width.dp,
                    height = screenBounds.height.dp
                )

                Window(
                    onCloseRequest = ::exitApplication,
                    title = "WannaCode - Editor",
                    undecorated = true,
                    state = windowState
                ) {
                    val window = this.window

                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background
                    ) {
                        Thread {
                            launchJdtServer(viewModel.getDir())
                        }.start()

                        IDEScreen(
                            directory = viewModel.getDir(),
                            window = window
                        )
                    }
                }
            }
        }
    }
}

suspend fun PointerInputScope.windowMovement(window: ComposeWindow) {
    var initialClick = Offset.Zero
    var initialWindowLocation = Point(0, 0)

    detectDragGestures(
        onDragStart = {
            val mousePos = MouseInfo.getPointerInfo().location
            initialClick = Offset(mousePos.x.toFloat(), mousePos.y.toFloat())
            initialWindowLocation = window.location
        },
        onDrag = { change, _ ->
            change.consume()
            val mousePos = MouseInfo.getPointerInfo().location
            val dx = mousePos.x - initialClick.x
            val dy = mousePos.y - initialClick.y
            window.setLocation(
                (initialWindowLocation.x + dx).toInt(),
                (initialWindowLocation.y + dy).toInt()
            )
        }
    )
}
