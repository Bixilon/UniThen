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

package de.bixilon.unithen.ui.main.checkin.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import de.bixilon.unithen.settings.Settings.SCAN_QR_AUTO_SCAN
import de.bixilon.unithen.settings.isSettingSupported
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.ui.containers.*
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.main.ScanQrAppointmentRoute
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil.SYNC_BACKOFF_NORMAL
import de.bixilon.unithen.ui.main.checkin.scan.attendees.ScanAttendeeList
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorageAsync
import de.bixilon.unithen.ui.sync.buttons.SyncFloatingButton
import de.bixilon.unithen.ui.sync.status.SyncStatusDialog
import de.bixilon.unithen.ui.sync.useRepeatedSyncEngine
import de.bixilon.unithen.ui.util.DelayedContent
import de.bixilon.unithen.ui.util.TimeFormatUtil.format
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useTime
import unithen.common.generated.resources.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun ScanAppointmentScreen(appointment: Appointment, info: Boolean = false) {
    val navigation = LocalNavigation.current
    val storage = LocalStorage.current

    val course = storage.courses[appointment.course]!!
    val account = storage.accounts.getTutorAccount(appointment)


    if (account == null) {
        SimpleErrorScreen(Res.string.scan_no_account_message.i18n(), Res.string.scan_no_account_title.i18n())
        return
    }

    val time = useTime()
    val canSync = appointment.canSyncCheckIn(time)

    val pending = rememberStorageAsync(appointment) { checkInQueue.getCount(appointment) } ?: 0
    val synchronize = useRepeatedSyncEngine(SYNC_BACKOFF_NORMAL + 1.minutes) {
        syncQueue(appointment)
    }

    val dialog = SyncStatusDialog(synchronize, Res.string.scan_synchronizing_attendees.i18n(), Res.string.scan_synchronizing_attendees.i18n(), manual = true)

    Screen {
        ScreenTitle(course.name)

        if (info) {
            InfoContainer {
                InfoPair(Res.string.appointment_start.i18n(), appointment.start.format())
                InfoPair(Res.string.appointment_end.i18n(), appointment.end.format())
                InfoPair(Res.string.appointment_location.i18n(), appointment.location)
            }
        }

        Box {
            ScanAttendeeList(appointment)

            FloatingActionButtons {
                if (canSync) {
                    if (pending > 0) {
                        DelayedContent(1.seconds) {
                            SyncFloatingButton(synchronize, Icons.Filled.Sync) { dialog.show() }
                        }
                    }
                }
                if (appointment.canPerformCheckIn() && isSettingSupported(SCAN_QR_AUTO_SCAN)) {
                    FloatingActionButton({ navigation.navigate(ScanQrAppointmentRoute(account, course, appointment)) }) {
                        Icon(Icons.Filled.QrCodeScanner, "scan")
                    }
                }
            }
        }
    }
}
