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

import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.api.graphql.types.checkin.CheckInAttemptQl
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

object CheckInUtil {
    val SYNC_BACKOFF = 5.minutes

    private suspend fun fetch(storage: SqlStorage, site: Site, account: Account, appointment: Appointment, user: User, attempt: CheckInAttempt) {
        val now = Clock.System.now()

        storage.checkInAttempts.update(appointment, user, sync = now)


        val attemptQl = withContext(Dispatchers.IO) {
            val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(account.session ?: ""))

            return@withContext api.checkInUser(appointment.uuid, user.uuid, appointment.uuid)
        }
        if (attemptQl == null) throw IllegalStateException("Null attempt?")

        attemptQl.user?.let { storage.users.add(site, it.id, it.firstName!!, it.lastName!!) }

        storage.checkInAttempts.add(appointment, user, uuid = attemptQl.id, message = attemptQl.message, sync = now, status = if (attemptQl.status == CheckInAttemptQl.Status.SUCCESS) CheckInAttempt.Status.OK else CheckInAttempt.Status.FAILED)
    }

    suspend fun checkIn(storage: SqlStorage, account: Account, appointment: Appointment, user: User): CheckInAttempt {
        val site = storage.sites[account.site]!!
        val now = Clock.System.now()

        val attempt = storage.checkInAttempts.add(appointment, user, now, now)

        fetch(storage, site, account, appointment, user, attempt)

        return storage.checkInAttempts[appointment, user]!!
    }

    fun checkOut(storage: SqlStorage, appointment: Appointment, user: User) {
        TODO("Implement")
    }


    suspend fun synchronizeDatabase(storage: SqlStorage, progress: (current: Int, total: Int) -> Unit) {
        val count = storage.checkInAttempts.getPendingSyncCount()

        if (count == 0) return

        var done = 0
        while (true) {
            val attempt = storage.checkInAttempts.takePendingSync() ?: break

            progress.invoke(done++, count)

            val user = storage.users[attempt.user]!!

            val appointment = storage.appointments[attempt.appointment]!!
            val course = storage.courses[appointment.course]!!
            val site = storage.sites[course.site]!!
            val account = storage.accounts.get(course).firstOrNull() ?: continue // TODO: only tutor accounts


            fetch(storage, site, account, appointment, user, attempt)
        }
    }
}
