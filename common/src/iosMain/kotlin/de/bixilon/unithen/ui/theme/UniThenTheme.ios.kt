package de.bixilon.unithen.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
actual fun UniThenTheme(darkTheme: Boolean, dynamicColor: Boolean, content: @Composable (() -> Unit)) {
    val colorScheme = when {
        darkTheme -> DARK_COLOR_SCHEME
        else -> LIGHT_COLOR_SCHEME
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
