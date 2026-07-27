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

import de.bixilon.unithen.api.errors.NetworkException
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchAttendees
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchEnrolled
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.sync.status.SyncProgressUpdate
import kotlinx.coroutines.*
import kotlin.time.Clock


class SyncEngine(
    val storage: SqlStorage,
    val onError: (Throwable) -> Unit,
) {

    // TODO: withPermit per request (don't duplicate them)

    private suspend fun handlErrors(progress: SyncProgressBuilder, block: suspend () -> Unit) {
        try {
            block.invoke()
            progress.addComplete()
        } catch (error: NetworkException) {
            error.printStackTrace()
            progress.addWarning()
        } catch (error: Exception) {
            error.printStackTrace()
            this@SyncEngine.onError.invoke(error)
            progress.addError()
        }
    }

    private suspend fun CoroutineScope.execute(progress: SyncProgressBuilder, block: suspend () -> Unit) = async(Dispatchers.IO) {
        handlErrors(progress, block)
    }

    suspend fun syncAttendees(appointments: List<Appointment>, force: Boolean = false, callback: (SyncProgressUpdate) -> Unit) {
        if (appointments.isEmpty()) return

        val now = Clock.System.now()

        val progress = SyncProgressBuilder(callback, appointments.size)

        coroutineScope {
            for (appointment in appointments) {
                if (!force && !appointment.isAttendeesStale(now)) {
                    progress.addSkipped()
                    continue
                }
                val account = storage.accounts.getTutorAccount(appointment)
                if (account == null) {
                    progress.addSkipped()
                    continue
                }

                execute(progress) { storage.fetchAttendees(account, appointment, false) }
            }
        }
    }

    suspend fun syncEnrolled(course: Course, force: Boolean = false) {
        val account = storage.accounts.getTutorAccount(course) ?: return
        storage.fetchEnrolled(account, course, force)
    }

    suspend fun syncAttendees(appointment: Appointment, force: Boolean = false) {
        val account = storage.accounts.getTutorAccount(appointment) ?: return
        storage.fetchAttendees(account, appointment, force)
    }
}
