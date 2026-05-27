/*
 * UniThen - Compose QR Camera
 * Copyright (C) 2026 Moritz Zwerger
 *
 * Ported from Activity to Jetpack Compose
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
            delay(10.seconds)
        }
    }

    LaunchedEffect(fakeTime) { time = getTime(fakeTime) }

    return time
}
