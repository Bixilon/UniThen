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
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.CheckInAttempt
import de.bixilon.unithen.storage.types.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock

object CheckInUtil {

    suspend fun checkIn(storage: SqlStorage, account: Account, appointment: Appointment, user: User): CheckInAttempt {
        val site = storage.sites[account.site]!!
        val now = Clock.System.now()

        storage.checkInAttempts.add(appointment, user, now, now)

        val attemptQl = withContext(Dispatchers.IO) {
            val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(account.session ?: ""))

            return@withContext api.checkInUser(appointment.uuid, user.uuid, appointment.uuid)
        }
        if (attemptQl == null) throw IllegalStateException("Null attempt?")

        attemptQl.user?.let { storage.users.add(site, it.id, it.firstName!!, it.lastName!!) }

        storage.checkInAttempts.add(appointment, user, uuid = attemptQl.id, message = attemptQl.message, sync = now, status = if (attemptQl.status == CheckInAttemptQl.Status.SUCCESS) CheckInAttempt.Status.OK else CheckInAttempt.Status.FAILED)

        return storage.checkInAttempts[appointment, user]!!
    }

    fun checkOut(storage: SqlStorage, appointment: Appointment, user: User) {
        TODO("Implement")
    }
}
