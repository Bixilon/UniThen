package de.bixilon.unithen.ui.util

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

actual fun Modifier.iosCloseKeyboardOnSwipe() = this
    .pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()

            if (dragAmount.y > 10) {
                forceCloseKeyboard()
            }
        }
    }
