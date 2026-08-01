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

package de.bixilon.unithen.ui.main.checkin.scan.qr.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.errors.NetworkException
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.main.checkin.scan.qr.QrScanResult
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.theme.checkInSuccess
import de.bixilon.unithen.ui.util.effects.RepeatedEffect
import de.bixilon.unithen.ui.util.useAsyncNetwork
import de.bixilon.unithen.ui.util.useHapticFeedback
import org.jetbrains.compose.resources.getString
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.error_network
import unithen.common.generated.resources.scan_unknown_error_server
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

val DEFAULT_DELAY = 30.seconds
val CHANGE_DELAY = 5.seconds

data class AcceptedState(
    val result: QrScanResult.Accepted,
) {
    var expires = TimeSource.Monotonic.markNow() + DEFAULT_DELAY
}

@Composable
fun rememberAcceptedStates(): SnapshotStateList<AcceptedState> {
    val accepted = remember { mutableStateListOf<AcceptedState>() }

    RepeatedEffect(100.milliseconds) {
        val now = TimeSource.Monotonic.markNow()
        accepted.removeAll { it.expires < now }
    }

    return accepted
}

@Composable
private fun AcceptedBox(state: AcceptedState, showCourseName: Boolean) {
    val storage = LocalStorage.current
    val haptic = useHapticFeedback()

    var message by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    val await by rememberSetting(Settings.SCAN_AWAIT_SERVER_CONFIRMATION)
    val offline by rememberSetting(Settings.SCAN_ALLOW_OFFLINE)


    val checkin = useAsyncNetwork(true) {
        if (!await) {
            state.expires = TimeSource.Monotonic.markNow() + CHANGE_DELAY
        }
        try {
            CheckInUtil.checkIn(storage, state.result.appointment, state.result.user)

            success = true
            haptic.invoke(HapticFeedbackType.Confirm)
        } catch (error: CheckInError) {
            haptic.invoke(HapticFeedbackType.Reject)
            message = getString(Res.string.scan_unknown_error_server, error.message ?: "")
        } catch (error: NetworkException) {
            if (offline) {
                success = true
            }
            message = getString(Res.string.error_network, error.message ?: "")
        } finally {
            if (await) {
                state.expires = TimeSource.Monotonic.markNow() + CHANGE_DELAY
            }
        }
    }


    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (success) checkInSuccess else if (message != null) MaterialTheme.colorScheme.errorContainer else checkInSuccess,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                when {
                    checkin.active -> CircularProgressIndicator()
                    message != null && success -> Icon(Icons.Filled.Warning, "")
                    message != null -> Icon(Icons.Filled.Close, "")
                    success -> Icon(Icons.Filled.Check, "")
                }

                Spacer(Modifier.width(8.dp))
                Text(text = state.result.user.fullname, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
            }

            if (showCourseName) {
                val course = rememberStorage { courses[state.result.appointment.course]!! }
                Text(text = course.name, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            }

            if (message != null) {
                Text(
                    text = message!!,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun AcceptedOverlay(accepted: List<AcceptedState>, showCourseName: Boolean = true) {
    if (accepted.isEmpty()) return


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 200.dp)
            .alpha(0.9f),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 300.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (state in accepted) {
                key(state.result) { AcceptedBox(state, showCourseName) }
            }
        }
    }
}
