package com.wannaverse.wannacode.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wannaverse.wannacode.theme.WannaCodeTheme

@Composable
fun PasteConflictDialog(
    fileName: String,
    onReplace: () -> Unit,
    onKeepBoth: () -> Unit,
    onSkip: () -> Unit
) {
    val colors = WannaCodeTheme.colors

    Dialog(onDismissRequest = onSkip) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .background(colors.surface, shape = RoundedCornerShape(8.dp))
                .border(1.dp, colors.border, shape = RoundedCornerShape(8.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "File Already Exists",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "\"$fileName\" already exists in this location. What would you like to do?",
                color = colors.textSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConflictOptionButton(
                    text = "Replace",
                    description = "Replace the existing file with the one you're pasting",
                    onClick = onReplace
                )

                ConflictOptionButton(
                    text = "Keep Both",
                    description = "Keep both files by renaming the pasted file",
                    onClick = onKeepBoth
                )

                ConflictOptionButton(
                    text = "Skip",
                    description = "Cancel this paste operation",
                    onClick = onSkip,
                    isSecondary = true
                )
            }
        }
    }
}

@Composable
private fun ConflictOptionButton(
    text: String,
    description: String,
    onClick: () -> Unit,
    isSecondary: Boolean = false
) {
    val colors = WannaCodeTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSecondary) colors.inputBackground else colors.accent.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                if (isSecondary) colors.border else colors.accent.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = text,
                color = if (isSecondary) colors.textSecondary else colors.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = colors.textTertiary,
                fontSize = 12.sp
            )
        }
    }
}
