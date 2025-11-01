package com.wannaverse.wannacode.common

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.SCROLL_BAR_IDLE_ACTIVE
import com.wannaverse.wannacode.SCROLL_BAR_IDLE_COLOR

@Composable
fun Scrollbar(scrollState: ScrollState, modifier: Modifier = Modifier) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        modifier = modifier.fillMaxHeight().width(8.dp),
        style = LocalScrollbarStyle.current.copy(
            unhoverColor = SCROLL_BAR_IDLE_COLOR,
            hoverColor = SCROLL_BAR_IDLE_ACTIVE,
            minimalHeight = 24.dp
        )
    )
}
