package de.bixilon.unithen.ui.fast

import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.ui.navigation.NavigationRoute


object FastCheckinHome : NavigationRoute

data class CheckInRoute(
    val account: Account,
    val course: Course,
    val appointment: Appointment,
) : NavigationRoute

data class CheckInAppointment(
    val course: Course,
    val appointment: Appointment,
) : NavigationRoute
