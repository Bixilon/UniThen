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
import de.bixilon.unithen.storage.StorageUtil.setCourses
import de.bixilon.unithen.storage.StorageUtil.storeAttendees
import de.bixilon.unithen.storage.StorageUtil.storeCourse
import de.bixilon.unithen.storage.StorageUtil.storeEnrolled
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.test.UniThenTestOnly
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import kotlinx.coroutines.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid


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

            async { syncAttendees(appointment, force) }
        }
    }


    suspend fun syncEnrolled(course: Course, force: Boolean = this.force) {
        val account = storage.accounts.getTutorAccount(course) ?: return
        if (!force && !course.isEnrolledStale()) return

        execute {
            val site = storage.sites[account.site]!!
            val api = account.api(site)


            val enrolled = api.getEnrolled(course.uuid)

            storage.storeEnrolled(site, course, enrolled!!)
        }
    }

    suspend fun syncAttendees(appointment: Appointment, force: Boolean = this.force) {
        val account = storage.accounts.getTutorAccount(appointment) ?: return
        if (!force && !appointment.isAttendeesStale()) return

        execute {
            val site = storage.sites[account.site]!!
            val api = account.api(site)


            val attemptsQl = api.getCheckInAttempts(appointment.uuid) ?: return@execute
            storage.storeAttendees(site, appointment, attemptsQl.attendees!!, attemptsQl.checkInAttempts!!)
        }
    }

    suspend fun syncCourse(course: Course, force: Boolean = this.force) {
        val account = storage.accounts.getTutorAccount(course) ?: storage.accounts[course].firstOrNull() ?: return

        if (!force && !course.isDataStale()) return

        execute {
            val site = storage.sites[account.site]!!
            val api = account.api(site)

            val detailsQl = api.getCourse(course.uuid)!!
            storage.storeCourse(site, detailsQl)

            if (storage.accounts.isTutor(account, course)) {
                val enrolled = api.getEnrolled(course.uuid)
                storage.storeEnrolled(site, course, enrolled!!)
            }
        }
    }

    private suspend fun syncCourse(account: Account, id: Uuid, tutor: Boolean) {
        val site = storage.sites[account.site]!!
        val api = account.api(site)

        val detailsQl = api.getCourse(id)!!

        val course = storage.storeCourse(site, detailsQl)
        storage.accounts.addToCourse(account, course, tutor)

        if (tutor) {
            val enrolled = api.getEnrolled(course.uuid)
            storage.storeEnrolled(site, course, enrolled!!)
        }
    }

    suspend fun syncCourses(account: Account, force: Boolean = this.force) = handlErrors {
        val site = storage.sites[account.site]!!
        val api = account.api(site)
        if (!force && !account.isStale()) return@handlErrors

        val enrolled: Set<Uuid>
        val tutor: Set<Uuid>

        coroutineScope {
            val _enrolled = async { api.getCourses(account.uuid, isEnrolled = true, isTutor = false)?.map { it.id }?.toSet() ?: emptySet() }
            val _tutor = async { api.getCourses(account.uuid, isEnrolled = false, isTutor = true)?.map { it.id }?.toSet() ?: emptySet() }
            enrolled = _enrolled.await()
            tutor = _tutor.await()
        }

        val all = enrolled + tutor

        storage.setCourses(account, site, all, tutor)

        coroutineScope {
            for (uuid in all) {
                val course = storage.courses[site, uuid]

                if (course != null && !course.isDataStale()) continue

                async(Dispatchers.IO) {
                    execute { syncCourse(account, uuid, uuid in tutor) }
                }
            }
        }

        storage.accounts.update(account.id, fetched = Clock.System.now())
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
