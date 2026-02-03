package de.bixilon.unithen.ui.fast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import de.bixilon.kutil.exception.Broken
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.main.CheckInScreen
import de.bixilon.unithen.ui.storage.LocalStorage

@Composable
fun FastCheckinAppointment(course: Course, appointment: Appointment) {
    val storage = LocalStorage.current
    val accounts by remember { storage.accounts.stateOf { this[course] } }

    when (accounts.size) {
        0 -> Broken("Unassociated data left in database!")
        1 -> CheckInScreen(accounts[0], course, appointment)
        else -> FastCheckinAccountSelector(course, appointment, accounts)
    }
}
