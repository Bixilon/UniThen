package de.bixilon.unithen.ui.util

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager


@Composable
fun Modifier.focusLoseOnTab(): Modifier {
    val focus = LocalFocusManager.current

    return this.pointerInput(Unit) {
        detectTapGestures {
            focus.clearFocus()
        }
    }
}
