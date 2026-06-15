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

package de.bixilon.unithen.ui.main.checkin.scan.qr

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.R
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInUnknownUserException
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.theme.checkInSuccess
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useAsyncNetwork
import okio.IOException
import kotlin.time.TimeSource


data class AcceptedState(
    val course: Course,
    val appointment: Appointment,
    val user: User,
) {
    val time = TimeSource.Monotonic.markNow()
}

@Composable
private fun AcceptedBox(state: AcceptedState) {
    val storage = LocalStorage.current
    val resources = LocalResources.current
    val haptic = LocalHapticFeedback.current
    val account = rememberStorage { storage.accounts.getTutorAccount(state.appointment) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var okay by remember { mutableStateOf(false) }


    val checkin = useAsyncNetwork<Unit>(account) {
        try {
            CheckInUtil.checkIn(storage, state.appointment, state.user)

            okay = true
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        } catch (_: CheckInUnknownUserException) {
            errorMessage = resources.getString(R.string.scan_unknown_user_server)
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
        } catch (error: CheckInError) {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            errorMessage = resources.getString(R.string.scan_unknown_error_server, error.message ?: "")
        } catch (error: IOException) {
            okay = true
            errorMessage = resources.getString(R.string.error_network, error.message ?: "")
        }
    }

    LaunchedEffect(Unit) { checkin.invoke(Unit) }


    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (okay) checkInSuccess else if (errorMessage != null) MaterialTheme.colorScheme.errorContainer else checkInSuccess,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = state.user.fullname, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
            Text(text = state.course.name, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)

            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                if (checkin.active) {
                    CircularProgressIndicator()
                    Spacer(Modifier.width(8.dp))
                    Text(R.string.scan_checking_in.i18n())
                } else {
                    val message = errorMessage ?: if (okay) R.string.scan_successful.i18n() else ""

                    var icon: ImageVector? = null

                    if (errorMessage != null) {
                        icon = Icons.Filled.Warning
                    } else if (okay) {
                        icon = Icons.Filled.Check
                    }
                    if (icon != null) {
                        Icon(icon, "")
                        Spacer(Modifier.width(8.dp))
                    }

                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun AcceptedOverlay(accepted: List<AcceptedState>) {
    if (accepted.isEmpty()) return


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 150.dp)
            .alpha(0.8f),
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
                key(state.user.id, state.appointment.id) { AcceptedBox(state) }
            }
        }
    }
}
