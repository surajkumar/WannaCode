package com.wannaverse.wannacode.ide.editor.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.ERROR_RED
import com.wannaverse.wannacode.common.Scrollbar
import com.wannaverse.wannacode.ide.editor.CodeEditorViewModel

@Composable
fun InfoPanel(
    viewModel: CodeEditorViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(viewModel.getLogs()) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Box(
        modifier = modifier
            .background(Color(0xFF111114))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            viewModel.getLogs().value.forEach { log ->
                Text(
                    text = log,
                    color = ERROR_RED
                )
            }
        }

        Scrollbar(
            scrollState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
