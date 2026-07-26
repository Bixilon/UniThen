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

package de.bixilon.unithen.sync

import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchAttendees
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.sync.status.SyncProgressUpdate
import kotlinx.coroutines.*
import kotlin.time.Clock


class SyncEngine(val storage: SqlStorage) {

    // TODO: withPermit per request (don't duplicate them)

    suspend fun syncAttendees(appointments: List<Appointment>, force: Boolean = false, callback: (SyncProgressUpdate) -> Unit) {
        if (appointments.isEmpty()) return

        val now = Clock.System.now()

        val progress = SyncProgressBuilder(callback, appointments.size)

        coroutineScope {
            appointments.mapNotNull {
                val site = storage.sites[storage.courses[it.course]!!.site]!!

                if (!it.isAttendeesStale(now)) {
                    progress.addSkipped()
                    return@mapNotNull null
                }
                val account = storage.accounts.getTutorAccount(it)
                if (account == null) {
                    progress.addSkipped()
                    return@mapNotNull null
                }

                SyncLock.withPermit(site.host) {
                    async(Dispatchers.IO) {
                        try {
                            storage.fetchAttendees(account, it, false)
                            progress.addComplete()
                        } catch (error: Exception) {
                            error.printStackTrace()
                            progress.addError() // TODO: distinguish with warning (network error)
                        }
                    }
                }
            }
        }.awaitAll()
    }
}
