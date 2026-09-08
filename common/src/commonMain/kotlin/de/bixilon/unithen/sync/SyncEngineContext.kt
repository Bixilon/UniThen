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
import de.bixilon.unithen.api.graphql.types.checkin.CheckInAttemptQl
import de.bixilon.unithen.storage.StorageUtil.setCourses
import de.bixilon.unithen.storage.StorageUtil.storeAttendees
import de.bixilon.unithen.storage.StorageUtil.storeCourse
import de.bixilon.unithen.storage.StorageUtil.storeEnrolled
import de.bixilon.unithen.storage.types.*
import de.bixilon.unithen.test.UniThenTestOnly
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInErrors
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

    val attendees = Attendees()
    val enrolled = Enrolled()
    val courses = Courses()
    val queue = Queue()

    @Deprecated("Not added to total")
    private suspend fun withErrorProgress(block: suspend () -> Unit) {
        try {
            block.invoke()
            progress.addComplete()
        } catch (error: NetworkException) {
            progress.addWarning()
            throw error
        } catch (error: Throwable) {
            progress.addError()
            throw error
        }
    }

    private suspend inline fun withProgress(noinline block: suspend () -> Unit) {
        progress.addTotal()
        withErrorProgress(block)
    }

    private suspend inline fun withDuplicateProgress(request: SyncEngineRequest, noinline block: suspend () -> Unit) {
        return engine.with(request) { withProgress(block) }
    }

    inner class Attendees {

        suspend fun sync(appointments: List<Appointment>, force: Boolean = this@SyncEngineContext.force) = coroutineScope {
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

                async { sync(appointment, force) }
            }
        }

        suspend fun sync(appointment: Appointment, force: Boolean = this@SyncEngineContext.force) {
            val account = storage.accounts.getTutorAccount(appointment) ?: return
            if (!force && !appointment.isAttendeesStale()) return

            withProgress {
                val site = storage.sites[account.site]
                val api = account.api(site)


                val attemptsQl = api.getCheckInAttempts(appointment.uuid) ?: return@withProgress
                storage.storeAttendees(site, appointment, attemptsQl.attendees!!, attemptsQl.checkInAttempts!!)
            }
        }
    }

    inner class Enrolled {

        suspend fun sync(course: Course, force: Boolean = this@SyncEngineContext.force) {
            val account = storage.accounts.getTutorAccount(course) ?: return
            if (!force && !course.isEnrolledStale()) return

            withProgress {
                val site = storage.sites[account.site]
                val api = account.api(site)


                val enrolled = api.getEnrolled(course.uuid)

                storage.storeEnrolled(site, course, enrolled!!)
            }
        }
    }

    inner class Courses {

        suspend fun sync(course: Course, force: Boolean = this@SyncEngineContext.force) {
            val account = storage.accounts.getTutorAccount(course) ?: storage.accounts[course].firstOrNull() ?: return

            if (!force && !course.isDataStale()) return

            withProgress {
                val site = storage.sites[account.site]
                val api = account.api(site)

                val detailsQl = api.getCourse(course.uuid)!!
                storage.storeCourse(site, detailsQl)

                if (storage.accounts.isTutor(account, course)) {
                    val enrolled = api.getEnrolled(course.uuid)
                    storage.storeEnrolled(site, course, enrolled!!)
                }
            }
        }

        private suspend fun sync(account: Account, id: Uuid, tutor: Boolean) {
            val site = storage.sites[account.site]
            val api = account.api(site)

            val detailsQl = api.getCourse(id)!!

            val course = storage.storeCourse(site, detailsQl)
            storage.accounts.addToCourse(account, course, tutor)

            if (tutor) {
                val enrolled = api.getEnrolled(course.uuid)
                storage.storeEnrolled(site, course, enrolled!!)
            }
        }

        suspend fun sync(account: Account, force: Boolean = this@SyncEngineContext.force) = withErrorProgress {
            val site = storage.sites[account.site]
            val api = account.api(site)
            if (!force && !account.isStale()) return@withErrorProgress

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
                        withProgress { sync(account, uuid, uuid in tutor) }
                    }
                }
            }

            storage.accounts.update(account.id, fetched = Clock.System.now())
        }

        suspend fun sync(force: Boolean = this@SyncEngineContext.force) = coroutineScope {
            for (account in storage.accounts.all()) {
                async { sync(account, force) }
            }
        }
    }

    inner class Queue {

        suspend fun sync(appointment: Appointment, force: Boolean = this@SyncEngineContext.force) = coroutineScope {
            var started = 0
            while (true) {
                val item = storage.checkInQueue.take(appointment, force) ?: break
                if (started++ > 30) {
                    delay(10.milliseconds)
                }

                async { sync(item) }
            }
        }

        private suspend fun sync(item: CheckInQueue) {
            if (item.attempt == null) {
                syncIn(item)
            } else {
                syncOut(item)
            }
        }

        private suspend fun syncIn(item: CheckInQueue) = withDuplicateProgress(CheckInQueueRequest(item.user, item.appointment)) {
            val _user = storage.users[item.user]
            val appointment = storage.appointments[item.appointment]
            val site = storage.sites[storage.courses[appointment.course].site]
            val account = storage.accounts.getTutorAccount(appointment) ?: return@withDuplicateProgress

            val api = account.api(site)

            val attemptQl = api.checkInUser(appointment.uuid, _user.uuid)!!

            val user = attemptQl.user?.let { storage.users.add(site, it.id, it.firstname!!, it.lastname!!) } ?: _user

            val error = attemptQl.error

            if (error == CheckInErrors.CheckInClosed && appointment.end > Clock.System.now()) return@withDuplicateProgress

            if (attemptQl.status != CheckInAttemptQl.Status.SUCCESS) {
                storage.checkInQueue.update(appointment, user, message = error?.message ?: "Unknown")

                throw CheckInError(error ?: CheckInErrors.Unknown)
            }
            storage.appointments.addAttendee(user, appointment, attemptQl.id) // TODO: Add to enrolled?
            storage.checkInQueue.delete(appointment, user)
        }

        private suspend fun syncOut(item: CheckInQueue) = withDuplicateProgress(CheckInQueueRequest(item.user, item.appointment)) {
            if (item.attempt == null) return@withDuplicateProgress

            val appointment = storage.appointments[item.appointment]
            val site = storage.sites[storage.courses[appointment.course].site]
            val account = storage.accounts.getTutorAccount(appointment) ?: return@withDuplicateProgress

            val attemptQl = account.api(site).deleteCheckInAttempt(item.attempt)!!

            val user = attemptQl.user?.let { storage.users.add(site, it.id, it.firstname!!, it.lastname!!) } ?: storage.users[item.user]

            // This should normally only be done if the request was successful, but maybe something is off (or the user is already checked out)
            storage.checkInQueue.delete(appointment, user)

            if (attemptQl.status != CheckInAttemptQl.Status.SUCCESS) {
                throw CheckInError(attemptQl.error ?: CheckInErrors.Unknown)
            }
        }


        suspend fun checkIn(appointment: Appointment, user: User) {
            val now = Clock.System.now()

            if (storage.checkInQueue[appointment, user] == null) {
                storage.transaction { it.checkInQueue.addPending(appointment, user, now) }
            } else {
                storage.checkInQueue.update(appointment, user, sync = now)
            }

            val item = storage.checkInQueue[appointment, user] ?: return

            queue.syncIn(item)
        }

        suspend fun checkOut(appointment: Appointment, user: User) {
            val attempt = storage.appointments.getAttemptId(appointment, user) ?: return

            storage.transaction {
                storage.appointments.removeAttendee(user, appointment)
                storage.checkInQueue.addCheckout(appointment, user, attempt, Clock.System.now())
            }

            val item = storage.checkInQueue[appointment, user] ?: return

            queue.syncOut(item)
        }
    }

    @UniThenTestOnly
    internal suspend fun test(block: suspend () -> Unit) = withProgress { block.invoke() }

    suspend fun async(block: suspend () -> Unit) {
        scope.async(Dispatchers.IO) { block.invoke() }
    }
}
