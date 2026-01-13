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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wannaverse.wannacode.STROKE_COLOR
import com.wannaverse.wannacode.common.Dropdown
import com.wannaverse.wannacode.common.PlainDropdown
import com.wannaverse.wannacode.ide.editor.viewmodel.CodeEditorViewModel
import com.wannaverse.wannacode.windowMovement
import org.jetbrains.compose.resources.painterResource
import wannacode.composeapp.generated.resources.Res
import wannacode.composeapp.generated.resources.close_cross
import wannacode.composeapp.generated.resources.minimize
import wannacode.composeapp.generated.resources.play

@Composable
fun Toolbar(viewModel: CodeEditorViewModel, window: ComposeWindow? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, STROKE_COLOR)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp
            )
            .then(
                if (window != null) {
                    Modifier.pointerInput(Unit) {
                        windowMovement(window)
                    }
                } else Modifier
            )
    ) {
        Row {
            Hamburger()

            Spacer(Modifier.width(30.dp))

            Dropdown(
                selectedOption = viewModel.projectName.value,
                options = listOf(),
                onOptionSelected = { },
                modifier = Modifier.width(200.dp).height(43.dp).padding(bottom = 10.dp).offset(y = (-5).dp),
                backgroundColor = Color(0xFF16161A),
                alignTextAlign = TextAlign.Center
            )

            Spacer(Modifier.width(20.dp))

            PlainDropdown(
                selectedOption = "Version Control",
                options = listOf("Pull", "Commit", "Push", "New Branch", "Change Branch"),
                onOptionSelected = {},
                modifier = Modifier.width(130.dp).offset(y = (-3).dp)
            )

            Spacer(Modifier.weight(1f))

            Dropdown(
                selectedOption = "./gradlew run",
                options = listOf(""),
                onOptionSelected = { },
                modifier = Modifier.width(200.dp).offset(y = (-10).dp).height(40.dp)
            )

            Spacer(Modifier.width(10.dp))

            Icon(
                painter = painterResource(Res.drawable.play),
                contentDescription = null,
                tint = Color(0xFF27FF27),
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(100.dp))

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
