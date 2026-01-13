package com.wannaverse.wannacode.ide.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.theme.WannaCodeTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.cross
import wannacode.composeapp.generated.resources.file
import wannacode.composeapp.generated.resources.right_cheveron

@Composable
fun CodeEditorTabs(viewModel: CodeEditorViewModel) {
    val colors = WannaCodeTheme.colors
    val tabIds = viewModel.orderedTabIds
    val currentTabId by viewModel.currentTab
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .height(50.dp)
            .border(1.dp, colors.tabBorder)
            .background(colors.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .clickable {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(
                            (scrollState.value - 150).coerceAtLeast(0)
                        )
                    }
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.right_cheveron),
                contentDescription = "Scroll left",
                tint = colors.textSecondary,
                modifier = Modifier.size(12.dp).rotate(180f)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabIds.forEach { tabId ->
                val tab = viewModel.tabContents[tabId]
                if (tab != null) {
                    val fileName = tab.file.name
                    val isSelected = currentTabId == tabId

                    Row(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) colors.tabBackgroundActive else colors.tabBackground,
                                shape = RoundedCornerShape(5.dp)
                            )
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = colors.tabBorderActive,
                                        shape = RoundedCornerShape(5.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { viewModel.showTab(tabId) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.file),
                            contentDescription = null,
                            tint = colors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = fileName,
                            color = if (isSelected) colors.tabTextActive else colors.tabText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            painter = painterResource(Res.drawable.cross),
                            contentDescription = "Close",
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { viewModel.closeTab(tabId) },
                            tint = colors.textDisabled
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .clickable {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(
                            (scrollState.value + 150).coerceAtMost(scrollState.maxValue)
                        )
                    }
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.right_cheveron),
                contentDescription = "Scroll right",
                tint = colors.textSecondary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
