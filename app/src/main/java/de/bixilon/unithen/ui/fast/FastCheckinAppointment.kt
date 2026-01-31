package de.bixilon.unithen.ui.fast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import de.bixilon.kutil.exception.Broken
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.STORAGE
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.main.CheckInScreen

@Composable
fun FastCheckinAppointment(course: Course, appointment: Appointment) {
    val accounts by remember { STORAGE.accounts.stateOf { this[course] } }

    when (accounts.size) {
        0 -> Broken("Unassociated data left in database!")
        1 -> CheckInScreen(accounts[0], course, appointment)
        else -> FastCheckinAccountSelector(course, appointment, accounts)
    }
}
