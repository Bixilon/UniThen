package de.bixilon.unithen.ui.util

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

actual fun Modifier.iosCloseKeyboardOnSwipe() = this
    .pointerInput(Unit) {
        var total = 0f

        detectDragGestures(
            onDragStart = { total = 0f },
            onDragCancel = { total = 0f },
            onDragEnd = { total = 0f },
            onDrag = { change, dragAmount ->
                change.consume()

                total += dragAmount.y

                if (total >= 50.0f) {
                    forceCloseKeyboard()
                    total = 0f
                }
            }
        )
    }
