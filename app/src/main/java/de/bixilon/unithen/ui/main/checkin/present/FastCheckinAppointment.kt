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

package de.bixilon.unithen.ui.main.checkin.present

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.storage.LocalStorage

@Composable
fun FastCheckinAppointment(course: Course, appointment: Appointment) {
    val storage = LocalStorage.current
    val accounts by remember { storage.accounts.stateOf { this[course] } }

    when (accounts.size) {
        0 -> SimpleErrorScreen("No account available")
        1 -> CheckInQrPresentScreen(accounts[0], course, appointment)
        else -> FastCheckinAccountSelector(course, appointment, accounts)
    }
}
