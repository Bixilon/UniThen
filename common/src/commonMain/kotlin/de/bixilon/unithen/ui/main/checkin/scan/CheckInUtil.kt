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

import de.bixilon.unithen.api.graphql.types.checkin.CheckInAttemptQl
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.CheckInQueue
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInErrors
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object CheckInUtil {
    val SYNC_BACKOFF_FORCE = 30.seconds
    val SYNC_BACKOFF_NORMAL = 5.minutes

    suspend fun syncQueue(storage: SqlStorage, item: CheckInQueue) {
        val user = storage.users[item.user]!!

        val appointment = storage.appointments[item.appointment]!!
        val course = storage.courses[appointment.course]!!
        val site = storage.sites[course.site]!!
        val account = storage.accounts.getTutorAccount(appointment) ?: return


        val attemptQl = account.api(site).checkInUser(appointment.uuid, user.uuid)!!

        attemptQl.user?.let { storage.users.add(site, it.id, it.firstname!!, it.lastname!!) }

        val error = attemptQl.error

        if (error == CheckInErrors.CheckInClosed && appointment.end > Clock.System.now()) return

        if (attemptQl.status != CheckInAttemptQl.Status.SUCCESS) {
            storage.checkInQueue.update(appointment, user, message = error?.message ?: "Unknown")

            throw CheckInError(error ?: CheckInErrors.Unknown)
        }
        storage.appointments.addAttendee(user, appointment, attemptQl.id) // TODO: Add to enrolled?
        storage.checkInQueue.delete(appointment, user)
    }

    private suspend fun sync(storage: SqlStorage, appointment: Appointment, user: User) {
        val now = Clock.System.now()

        if (storage.checkInQueue[appointment, user] == null) {
            storage.transaction { it.checkInQueue.addPending(appointment, user, now) }
        } else {
            storage.checkInQueue.update(appointment, user, sync = now)
        }

        val item = storage.checkInQueue[appointment, user] ?: return

        syncQueue(storage, item)
    }

    suspend fun checkIn(storage: SqlStorage, appointment: Appointment, user: User) {
        sync(storage, appointment, user)
    }

    suspend fun checkOut(storage: SqlStorage, appointment: Appointment, user: User) {
        val course = storage.courses[appointment.course]!!
        val site = storage.sites[course.site]!!
        val account = storage.accounts.getTutorAccount(appointment) ?: return

        val attempt = storage.appointments.getAttemptId(appointment, user) ?: return

        storage.transaction {
            storage.appointments.removeAttendee(user, appointment)
            storage.checkInQueue.addCheckout(appointment, user, attempt, Clock.System.now())
        }


        val attemptQl = account.api(site).deleteCheckInAttempt(attempt)!!

        attemptQl.user?.let { storage.users.add(site, it.id, it.firstname!!, it.lastname!!) }

        storage.checkInQueue.delete(appointment, user) // TODO: Delte after checking status?

        if (attemptQl.status != CheckInAttemptQl.Status.SUCCESS) {
            throw CheckInError(attemptQl.error ?: CheckInErrors.Unknown)
        }
    }
}
