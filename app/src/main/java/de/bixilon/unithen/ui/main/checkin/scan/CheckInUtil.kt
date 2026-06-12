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
import de.bixilon.unithen.storage.types.*
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInUnknownUserException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

object CheckInUtil {
    val MAJOR_CONTRIBUTORS = mutableMapOf(
        0x54550CBADB5BC304 to "moritz",
    )
    val SYNC_BACKOFF = 5.minutes

    suspend fun syncQueue(storage: SqlStorage, item: CheckInQueue) {
        val user = storage.users[item.user]!!

        val appointment = storage.appointments[item.appointment]!!
        val course = storage.courses[appointment.course]!!
        val site = storage.sites[course.site]!!
        val account = storage.accounts.getTutorAccount(course) ?: return


        val attemptQl = withContext(Dispatchers.IO) {
            val api = account.api(site)

            return@withContext api.checkInUser(appointment.uuid, user.uuid)
        }

        if (attemptQl == null) throw IllegalStateException("Null attempt?")

        attemptQl.user?.let { storage.users.add(site, it.id, it.firstname!!, it.lastname!!) }

        if (attemptQl.error == CheckInAttemptQl.Error.CHECKIN_CLOSED && appointment.end > Clock.System.now()) return

        if (attemptQl.status != CheckInAttemptQl.Status.SUCCESS) {
            storage.checkInQueue.update(appointment, user, message = attemptQl.message ?: "Unknown")

            throw CheckInError(attemptQl.message)
        }
        storage.appointments.addAttendee(user, appointment, attemptQl.id) // TODO: Add to enrolled?
        storage.checkInQueue.delete(appointment, user)
    }

    private suspend fun sync(storage: SqlStorage, appointment: Appointment, user: User) {
        val now = Clock.System.now()

        if (storage.checkInQueue[appointment, user] == null) {
            storage.checkInQueue.addPending(appointment, user, now)
        } else {
            storage.checkInQueue.update(appointment, user, sync = now)
        }

        val item = storage.checkInQueue[appointment, user] ?: return

        syncQueue(storage, item)
    }

    private suspend fun syncUnknownUser(storage: SqlStorage, site: Site, account: Account, appointment: Appointment, userId: Uuid) {
        val attemptQl = withContext(Dispatchers.IO) {
            val api = account.api(site)

            return@withContext api.checkInUser(appointment.uuid, userId)
        }
        if (attemptQl == null) return
        if (attemptQl.user == null) {
            throw CheckInUnknownUserException(attemptQl.message)
        }

        val user = attemptQl.user.let { storage.users.add(site, it.id, it.firstname!!, it.lastname!!) }

        if (attemptQl.status != CheckInAttemptQl.Status.SUCCESS) {
            throw CheckInError(attemptQl.message)
        }

        storage.appointments.addAttendee(user, appointment, attemptQl.id)  // TODO: Add to enrolled?
    }

    suspend fun checkIn(storage: SqlStorage, appointment: Appointment, user: User) {
        sync(storage, appointment, user)
    }

    suspend fun checkIn(storage: SqlStorage, account: Account, appointment: Appointment, userId: Uuid) {
        val site = storage.sites[account.site]!!

        syncUnknownUser(storage, site, account, appointment, userId)
    }

    suspend fun checkOut(storage: SqlStorage, appointment: Appointment, user: User) {
        val course = storage.courses[appointment.course]!!
        val site = storage.sites[course.site]!!
        val account = storage.accounts.getTutorAccount(course) ?: return

        val attempt = storage.appointments.getAttemptId(appointment, user) ?: return

        storage.transaction {
            storage.appointments.removeAttendee(user, appointment)
            storage.checkInQueue.addCheckout(appointment, user, attempt, Clock.System.now())
        }


        val attemptQl = withContext(Dispatchers.IO) {
            val api = account.api(site)

            return@withContext api.deleteCheckInAttempt(attempt)
        }

        if (attemptQl == null) throw IllegalStateException("Null attempt?")

        attemptQl.user?.let { storage.users.add(site, it.id, it.firstname!!, it.lastname!!) }

        storage.checkInQueue.delete(appointment, user) // TODO: Delte after checking status?

        if (attemptQl.status != CheckInAttemptQl.Status.SUCCESS) {
            throw CheckInError(attemptQl.message)
        }
    }


    suspend fun synchronizeDatabase(storage: SqlStorage, progress: (current: Int, total: Int) -> Unit) {
        val count = storage.checkInQueue.count

        if (count == 0) return

        var done = 0
        while (true) {
            val item = storage.checkInQueue.take() ?: break

            progress.invoke(done++, count)

            syncQueue(storage, item)
        }
    }


    fun isMajorContributor(user: User): Boolean {
        // Well, not the best, but not revealing my user id :)
        // This is just an e*as*ter eg*g, nothing special. Purely visual.
        val hash = user.uuid.toLongs { a, b -> a xor b } and 0xFB.inv()

        val name = MAJOR_CONTRIBUTORS[hash] ?: return false

        return user.firstname.lowercase().trim() == name
    }
}
