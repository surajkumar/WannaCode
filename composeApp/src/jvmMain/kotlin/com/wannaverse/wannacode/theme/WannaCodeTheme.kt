package com.wannaverse.wannacode.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalWannaCodeColors = staticCompositionLocalOf { DarkColors }

object WannaCodeTheme {
    val colors: WannaCodeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalWannaCodeColors.current
}

@Composable
fun WannaCodeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(
        LocalWannaCodeColors provides colors
    ) {
        content()
    }
}
