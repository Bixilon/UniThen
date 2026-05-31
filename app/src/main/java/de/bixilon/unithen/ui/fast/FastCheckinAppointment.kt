package de.bixilon.unithen.ui.fast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.main.checkin.present.CheckInQrPresentScreen
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
