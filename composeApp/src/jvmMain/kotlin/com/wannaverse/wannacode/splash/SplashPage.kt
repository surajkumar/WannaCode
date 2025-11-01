package com.wannaverse.wannacode.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wannaverse.wannacode.PRIMARY_BG_COLOR
import com.wannaverse.wannacode.splash.components.HeaderBar
import com.wannaverse.wannacode.splash.components.Menu
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun SplashPage(viewModel: SplashPageViewModel = viewModel { SplashPageViewModel() }, hideSplash: () -> Unit) {
    Column(
        modifier = Modifier
            .background(PRIMARY_BG_COLOR)
            .fillMaxSize()
    ) {
        HeaderBar()
        Menu(viewModel, hideSplash)
    }
}
