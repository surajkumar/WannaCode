package com.wannaverse.wannacode.ide

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.STROKE_COLOR
import com.wannaverse.wannacode.common.Dropdown
import com.wannaverse.wannacode.common.PlainDropdown
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.close_cross
import wannacode.composeapp.generated.resources.minimize
import wannacode.composeapp.generated.resources.play

@Composable
fun Toolbar(viewModel: CodeEditorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, STROKE_COLOR)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp
            )
    ) {
        Row(modifier = Modifier.padding(bottom = 20.dp)) {
            Hamburger()

            Spacer(Modifier.weight(1f))

            Icon(
                painter = painterResource(Res.drawable.minimize),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(top = 1.dp).size(10.dp)
            )

            Spacer(Modifier.width(15.dp))

            Icon(
                painter = painterResource(Res.drawable.close_cross),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}
