package com.wannaverse.wannacode.ide.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.theme.WannaCodeTheme
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.cross
import wannacode.composeapp.generated.resources.file

@Composable
fun CodeEditorTabs(viewModel: CodeEditorViewModel) {
    val colors = WannaCodeTheme.colors
    val tabIds = viewModel.orderedTabIds
    val currentTabId by viewModel.currentTab
    val selectedTabIndex = tabIds.indexOf(currentTabId).coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        edgePadding = 8.dp,
        backgroundColor = Color.Transparent,
        contentColor = colors.textPrimary,
        indicator = {},
        divider = {},
        modifier = Modifier.border(1.dp, colors.tabBorder)
    ) {
        tabIds.forEachIndexed { _, tabId ->
            val tab = viewModel.tabContents[tabId]
            if (tab != null) {
                val fileName = tab.file.name

                val isSelected = currentTabId == tabId

                val tabModifier = Modifier
                    .padding(5.dp)
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

                Tab(
                    selected = isSelected,
                    onClick = {
                        viewModel.showTab(tabId)
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.file),
                                contentDescription = null,
                                tint = colors.textTertiary
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
                                    .padding(start = 20.dp, top = 4.dp)
                                    .size(16.dp)
                                    .clickable {
                                        viewModel.closeTab(tabId)
                                    },
                                tint = colors.textDisabled
                            )
                        }
                    },
                    modifier = tabModifier
                )
            }
        }
    }
}
