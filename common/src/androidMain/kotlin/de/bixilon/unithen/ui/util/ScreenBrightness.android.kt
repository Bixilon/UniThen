/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen.ui.util

import android.content.Context
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import de.bixilon.unithen.util.AndroidUtil.activity

private fun setBrightness(context: Context, level: Float) {
    val window = context.activity?.window ?: return

    window.attributes = window.attributes.apply { screenBrightness = level }
}

@Composable
actual fun ScreenBrightnessOverride(value: Float) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        setBrightness(context, value)

        onDispose {
            setBrightness(context, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        }
    }
}
