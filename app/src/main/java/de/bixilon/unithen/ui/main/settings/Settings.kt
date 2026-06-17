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

package de.bixilon.unithen.ui.main.settings

import de.bixilon.unithen.ui.main.MainScreens
import de.bixilon.unithen.ui.main.checkin.scan.attendees.AttendeeSort
import de.bixilon.unithen.ui.main.checkin.scan.attendees.Order

object Settings {
    val QR_CODE_REMOVE_NAME = Setting("qr_code_fake_name", false)
    val FAKE_TIME = Setting("fake_time", false)

    val SCAN_QR_HIGH_RESOLUTION = Setting("scan_qr_high_resolution", false)
    val SCAN_QR_AUTO_SCAN = Setting("scan_qr_auto_scan", true)
    val SCAN_AWAIT_SERVER_CONFIRMATION = Setting("scan_await_server_confirmation", true)
    val SCAN_CONFIRMATION_SCREEN = Setting("scan_confirmation_screen", true)

    val FETCH_APPOINTMENTS = Setting("fetch_appointments", false)

    val ENTRYPOINT = Setting("entrypoint", MainScreens.COURSES)

    val ATTENDEE_ORDER = Setting("attendee_order", Order.ASC)
    val ATTENDEE_SORT = Setting("attendee_sort", AttendeeSort.LASTNAME)
}
