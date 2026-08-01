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

package de.bixilon.unithen.ui.main.checkin.scan.qr.confirm

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.main.checkin.scan.qr.QrScanResult
import de.bixilon.unithen.ui.main.checkin.scan.qr.QrScanUtil
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.state.rememberStateOf
import kotlin.uuid.Uuid

@Composable
private fun ColumnScope.ScanQrConfirmScreenContent(appointment: Appointment, user: User?) {
    val navigation = LocalNavigation.current
    val course = rememberStorage { courses[appointment.course]!! }

    if (user == null) return ScanQrNotEnrolled(null, course, appointment)

    val result = rememberStorage { QrScanUtil.scan(this, appointment, user) }

    if (result is QrScanResult.NotEnrolled) {
        return ScanQrNotEnrolled(user, course, appointment)
    }

    val await by rememberSetting(Settings.SCAN_AWAIT_SERVER_CONFIRMATION)

    var success by rememberStateOf(false)
    var loading by rememberStateOf(false)
    var error by rememberStateOf<String?>(null)

    if (!await && (success || error != null)) {
        LaunchedEffect(Unit) { navigation.pop() }
        return
    }

    if (success) return ScanQrConfirmed(user, appointment)
    if (error != null) return ScanQrError(user, error!!, appointment)
    if (loading) return ScanQrLoading(user, appointment)

    if (result is QrScanResult.Accepted) {
        return ScanQrAwait(user, appointment, { loading = it }, { success = true }, onError = { error = it })
    }
    if (result !is QrScanResult.SoftError) {
        return SimpleErrorScreen("Unknown QR result", result::class.simpleName)
    }
    return ScanQrError(user, result)
}

@Composable
fun ScanQrConfirmScreen(appointment: Appointment, user: User?) {
    val course = rememberStorage { courses[appointment.course]!! }


    Screen(horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTitle(course.name)
        Spacer(Modifier.height(8.dp))

        ScanQrConfirmScreenContent(appointment, user)
    }
}

@Composable
fun ScanQrConfirmScreen(appointment: Appointment, userId: Uuid) {
    val course = rememberStorage { courses[appointment.course]!! }
    val user = rememberStorage { users[sites[course.site]!!, userId] }

    ScanQrConfirmScreen(appointment, user)
}
