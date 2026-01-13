package com.wannaverse.wannacode.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wannaverse.wannacode.splash.components.HeaderBar
import com.wannaverse.wannacode.splash.components.Menu
import com.wannaverse.wannacode.theme.WannaCodeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun SplashPage(viewModel: SplashPageViewModel = viewModel { SplashPageViewModel() }, hideSplash: () -> Unit) {
    val colors = WannaCodeTheme.colors

    Column(
        modifier = Modifier
            .background(colors.background)
            .fillMaxSize()
    ) {
        HeaderBar()
        Menu(viewModel, hideSplash)
    }
}
