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
import de.bixilon.unithen.api.graphql.util.CourseFetcher.updateCourse
import de.bixilon.unithen.api.graphql.util.CourseFetcher.updateCourses
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.test.UniThenTestOnly
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import kotlinx.coroutines.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds


class SyncEngineContext(
    private val engine: SyncEngine,
    private val force: Boolean,
    private val scope: CoroutineScope,
    progress: (SyncEngineProgress) -> Unit,
) {
    private val storage get() = engine.storage
    private val progress = SyncProgressBuilder(progress)

    private suspend fun handlErrors(block: suspend () -> Unit) {
        try {
            block.invoke()
            progress.addComplete()
        } catch (error: NetworkException) {
            error.printStackTrace()
            progress.addWarning()
        } catch (error: Exception) {
            error.printStackTrace()
            engine.onError.invoke(error)
            progress.addError()
        }
    }

    private suspend inline fun execute(noinline block: suspend () -> Unit) {
        progress.addTotal()
        handlErrors(block)
    }


    suspend fun syncAttendees(appointments: List<Appointment>, force: Boolean = this.force) = coroutineScope {
        if (appointments.isEmpty()) return@coroutineScope

        val now = Clock.System.now()

        progress.addTotal(appointments.size)

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

            async { storage.fetchAttendees(account, appointment, force) }
        }
    }

    suspend fun syncEnrolled(course: Course, force: Boolean = this.force) {
        val account = storage.accounts.getTutorAccount(course) ?: return
        storage.fetchEnrolled(account, course, force)
    }

    suspend fun syncAttendees(appointment: Appointment, force: Boolean = this.force) {
        val account = storage.accounts.getTutorAccount(appointment) ?: return
        execute { storage.fetchAttendees(account, appointment, force) }
    }

    suspend fun syncCourse(course: Course) {
        val account = storage.accounts.getTutorAccount(course) ?: storage.accounts[course].firstOrNull() ?: return

        execute { storage.updateCourse(account, course) }
    }

    suspend fun syncCourses(account: Account, force: Boolean = this.force) = execute {
        storage.updateCourses(account, force)
    }

    suspend fun syncCourses(force: Boolean = this.force) = coroutineScope {
        for (account in storage.accounts.all()) {
            async { syncCourses(account, force) }
        }
    }

    suspend fun syncQueue(appointment: Appointment, force: Boolean = this.force) = coroutineScope {
        var started = 0
        while (true) {
            val item = storage.checkInQueue.take(appointment, force) ?: break
            if (started++ > 30) {
                delay(10.milliseconds)
            }

            async { execute { CheckInUtil.syncQueue(storage, item) } }
        }
    }

    @UniThenTestOnly
    internal suspend fun test(block: suspend () -> Unit) = execute { block.invoke() }

    suspend fun async(block: suspend () -> Unit) {
        scope.async(Dispatchers.IO) { block.invoke() }
    }
}
