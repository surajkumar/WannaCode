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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wannaverse.wannacode.ide.IDEScreen
import com.wannaverse.wannacode.splash.SplashPage
import com.wannaverse.wannacode.splash.SplashPageViewModel
import java.awt.GraphicsEnvironment
import java.io.File
import kotlinx.coroutines.launch

val theme = darkColorScheme(
    primary = PRIMARY_BG_COLOR,
    onPrimary = Color.White,
    primaryContainer = PRIMARY_BG_COLOR,
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    secondaryContainer = PRIMARY_BG_COLOR,
    onSecondaryContainer = Color.White,
    tertiaryContainer = Color.Blue,
    onTertiaryContainer = Color.White,
    background = PRIMARY_BG_COLOR,
    onBackground = Color.White,
    surface = PRIMARY_BG_COLOR,
    onSurface = Color.White,
    onSurfaceVariant = Color.White,
    surfaceContainer = PRIMARY_BG_COLOR,
    outline = Color(0xff303c4c),
    error = Color.Red
)

fun main() = application {
    var showSplash by remember { mutableStateOf(true) }

    MaterialTheme(
        colorScheme = theme
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
                val scope = rememberCoroutineScope()

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val location = window.location
                                    scope.launch {
                                        window.setLocation(
                                            location.x + dragAmount.x.toInt(),
                                            location.y + dragAmount.y.toInt()
                                        )
                                    }
                                }
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
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    IDEScreen(viewModel.getDir())
                }
            }
        }
    }
}
