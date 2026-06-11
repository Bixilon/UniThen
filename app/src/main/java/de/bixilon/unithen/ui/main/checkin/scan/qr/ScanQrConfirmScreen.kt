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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.R
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchEnrolled
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.CheckInQueue
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.containers.InfoPair
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import de.bixilon.unithen.ui.main.checkin.scan.LocalScanContext
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInUnknownUserException
import de.bixilon.unithen.ui.main.settings.Settings
import de.bixilon.unithen.ui.main.settings.rememberSetting
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.TimeFormatUtil.format
import de.bixilon.unithen.ui.util.TimeFormatUtil.formatNow
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useAsyncNetwork
import okio.IOException
import kotlin.uuid.Uuid


val MAJOR_CONTRIBUTORS = mutableMapOf(
    0x54550CBADB5BC304 to "moritz",
)

fun isMajorContributor(user: User): Boolean {
    // Well, not the best, but not revealing my user id :)
    // This is just an e*as*ter eg*g, nothing special. Purely visual.
    val hash = user.uuid.toLongs { a, b -> a xor b } and 0xFB.inv()

    val name = MAJOR_CONTRIBUTORS[hash] ?: return false

    return user.firstname.lowercase().trim() == name
}


@Composable
private fun Warning(confirming: Boolean, user: User?, enrolled: Boolean, attendee: Boolean, attempt: CheckInQueue?, message: String?) {
    val size = Modifier
        .height(200.dp)
        .width(200.dp)

    if (confirming) {
        return CircularProgressIndicator(modifier = size)
    }


    when {
        user == null -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
        !enrolled -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
        (attempt != null && attempt.message != null) || attendee -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
        attempt != null -> Icon(Icons.Filled.Warning, "", tint = Color.Yellow, modifier = size)

        else -> Icon(Icons.Filled.CheckCircle, "", tint = Color(0xFF00A000), modifier = size) // dark green
    }

    if (message != null || attempt?.message != null) {
        return ErrorBox(message ?: attempt?.message ?: "Error")
    }

    val warning = when {
        user == null -> R.string.scan_error_unknown_user.i18n()
        !enrolled -> R.string.scan_error_not_enrolled.i18n()
        attempt != null && attempt.attempt == null -> R.string.scan_error_already_checked_in_pending_checkout.i18n()
        attendee -> R.string.scan_error_already_checked_in.i18n()
        attempt != null -> R.string.scan_error_already_checked_in_pending.i18n()
        else -> null
    }

    warning?.let { ErrorBox(it) }
}

@Composable
private fun EnrolledListWarning(account: Account, course: Course) {
    val storage = LocalStorage.current

    if (!course.isEnrolledStale()) return

    var updating by remember { mutableStateOf(false) }
    val refresh = useAsyncNetwork<Unit>(account) {
        try {
            updating = true
            storage.fetchEnrolled(account, course, false)
        } finally {
            updating = false
        }
    }

    LaunchedEffect(Unit) { refresh.invoke(Unit) }

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        if (updating) {
            CircularProgressIndicator(modifier = Modifier.padding(horizontal = 16.dp)); Text(R.string.scan_updating_enrolled.i18n())
        } else {
            Icon(Icons.Default.Warning, "", tint = Color.Yellow); Spacer(Modifier.width(16.dp)); Text(R.string.scan_enrolled_outdated.i18n(course.fetched.formatNow()))
        }
    }
}


@Composable
fun ScanQrConfirmScreen(user: User?, userId: Uuid) {
    val haptic = LocalHapticFeedback.current
    val navigation = LocalNavigation.current
    val storage = LocalStorage.current
    val (account, course, appointment) = LocalScanContext.current

    val enrolled = user?.let { rememberStorage { users.isEnrolled(course, user) } } ?: false
    val attendee = rememberStorage { user?.let { users.isAttendee(appointment, user) } } ?: false
    val queue = rememberStorage { user?.let { checkInQueue[appointment, user] } }

    var message by remember { mutableStateOf<String?>(null) }

    val auto by rememberSetting(Settings.SCAN_QR_AUTO_SCAN)


    val resources = LocalResources.current
    val checkin = useAsyncNetwork<Unit>(account) {
        try {
            if (user == null) {
                CheckInUtil.checkIn(storage, account, appointment, userId)
            } else {
                CheckInUtil.checkIn(storage, appointment, user)
            }

            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            navigation.pop(); if (!auto) navigation.pop() // close scanner too
        } catch (error: IOException) {
            if (user == null) {
                message = resources.getString(R.string.network_error)
            } else {
                navigation.pop(); if (!auto) navigation.pop() // close scanner too
            }
            throw error
        } catch (_: CheckInUnknownUserException) {
            message = resources.getString(R.string.scan_unknown_user_server)
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
        } catch (error: CheckInError) {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            message = resources.getString(R.string.scan_unknown_error_server, error.message ?: "")
        }
    }

    Screen(horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTitle(course.name)

        Warning(checkin.active, user, enrolled, attendee, queue, message)
        Spacer(Modifier.height(16.dp))


        if (user == null) {
            EnrolledListWarning(account, course)
            Spacer(Modifier.height(16.dp))
        }

        InfoContainer(modifier = Modifier.fillMaxWidth(0.8f)) {
            user?.let { InfoPair(R.string.user_name.i18n(), "${user.firstname} ${user.lastname}") }
            InfoPair(R.string.appointment_start.i18n(), appointment.start.format())
            InfoPair(R.string.appointment_end.i18n(), appointment.end.format())
            InfoPair(R.string.appointment_location.i18n(), appointment.location)
        }

        Spacer(Modifier
            .weight(1.0f)
            .defaultMinSize(minHeight = 16.dp))


        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Button({
                navigation.pop()
            }, enabled = !checkin.active, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                Icon(Icons.Filled.Close, "cancel")
                Text(R.string.cancel.i18n())
            }

            if (queue != null && queue.message == null) {
                Button({ checkin.invoke(Unit) }, enabled = !checkin.active, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onErrorContainer)) {
                    Icon(Icons.Filled.Sync, "synchronize")
                    Text(R.string.scan_try_synchronize.i18n())
                }
            }

            Button({ checkin.invoke(Unit) }, enabled = message == null && !checkin.active && !attendee && queue == null, modifier = Modifier.fillMaxWidth()) {
                if (user == null || !enrolled) { // TODO: danger button color?
                    Icon(Icons.Filled.Warning, "check")
                    Text(R.string.scan_try_anyways.i18n())
                } else {
                    Icon(Icons.Filled.Check, "check")
                    Text(stringResource(if (isMajorContributor(user)) R.string.scan_confirm_contributor else R.string.scan_confirm))
                }
            }
        }
    }
}

@Composable
fun ScanQrConfirmScreen(userId: Uuid) {
    val (_, _, appointment) = LocalScanContext.current

    val storage = LocalStorage.current
    val site = storage.sites[storage.courses[appointment.course]!!.site]!!
    val user = storage.users[site, userId]

    ScanQrConfirmScreen(user, userId)
}
