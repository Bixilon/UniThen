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

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

val CONFIRM by lazy { VibrationEffect.createOneShot(80, 100) }
val REJECT by lazy { VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE) }

@Composable
fun useHapticFeedback(): (HapticFeedbackType) -> Unit {
    val vibrator = LocalContext.current.getSystemService(Vibrator::class.java)
    val haptics = LocalHapticFeedback.current

    // TODO: This is not great, but on my device HapticFeedbackType.Confirm, ... does not work (only LONG_PRESS)
    return {
        when (it) {
            HapticFeedbackType.Confirm -> vibrator.vibrate(CONFIRM)
            HapticFeedbackType.Reject -> vibrator.vibrate(REJECT)
            else -> haptics.performHapticFeedback(it)
        }
    }
}
