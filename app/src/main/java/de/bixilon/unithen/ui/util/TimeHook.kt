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

import androidx.compose.runtime.*
import de.bixilon.unithen.ui.main.settings.Settings
import de.bixilon.unithen.ui.main.settings.rememberSetting
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun getTime(fake: Boolean) = if (fake) Instant.fromEpochSeconds(1769446901) else Clock.System.now()

@Composable
fun useTime(): Instant {
    val fakeTime by rememberSetting(Settings.FAKE_TIME)
    var time by remember { mutableStateOf(getTime(fakeTime)) }

    LaunchedEffect(Unit) {
        while (true) {
            time = getTime(fakeTime)
            delay(30.seconds)
        }
    }

    LaunchedEffect(fakeTime) { time = getTime(fakeTime) }

    return time
}
