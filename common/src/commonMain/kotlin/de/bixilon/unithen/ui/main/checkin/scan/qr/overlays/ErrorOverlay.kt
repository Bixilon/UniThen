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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.main.checkin.scan.qr.QrScanResult
import de.bixilon.unithen.ui.util.TimeFormatUtil
import de.bixilon.unithen.ui.util.effects.RepeatedEffect
import de.bixilon.unithen.ui.util.i18n
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

data class ErrorState(
    val result: QrScanResult.Error,
) {
    val expires = TimeSource.Monotonic.markNow() + 1.seconds
}

@Composable
fun rememberErrorStates(): SnapshotStateSet<ErrorState> {
    val errors = remember { mutableStateSetOf<ErrorState>() }

    RepeatedEffect(100.milliseconds) {
        val now = TimeSource.Monotonic.markNow()
        errors.removeAll { it.expires < now }
    }

    return errors
}

@Composable
private fun Error(state: ErrorState) {
    val details = when (state.result) {
        is QrScanResult.AlreadyCheckedIn -> state.result.user.fullname
        is QrScanResult.CheckInPending -> state.result.user.fullname
        is QrScanResult.CheckOutPending -> state.result.user.fullname
        is QrScanResult.NotEnrolled -> state.result.user.fullname
        is QrScanResult.Rejected -> state.result.message
        is QrScanResult.WrongAppointment -> TimeFormatUtil.formatTimespam(state.result.appointment.start, state.result.appointment.end)
        is QrScanResult.WrongCourse -> state.result.course.name
        else -> null
    }
    ErrorBox(state.result.label.i18n(), details)
    // TODO: Show expiring timer
}


@Composable
fun ErrorOverlay(errors: Set<ErrorState>) {
    if (errors.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(bottom = 50.dp)
            .alpha(0.7f),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 300.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (error in errors) {
                Error(error)
            }
        }
    }
}
