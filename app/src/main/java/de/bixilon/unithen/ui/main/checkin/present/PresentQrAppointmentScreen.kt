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
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.storage.rememberStorage

@Composable
fun PresentQrAppointmentScreen(course: Course, appointment: Appointment) {
    val accounts = rememberStorage { accounts[course] }

    when (accounts.size) {
        0 -> SimpleErrorScreen("No account available")
        1 -> PresentQrScreen(accounts[0], course, appointment)
        else -> PresentQrAccountSelector(course, appointment, accounts)
    }
}
