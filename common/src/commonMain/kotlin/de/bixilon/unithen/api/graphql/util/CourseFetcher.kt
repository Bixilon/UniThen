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

package de.bixilon.unithen.api.graphql.util

import de.bixilon.unithen.api.graphql.types.CourseQl
import de.bixilon.unithen.api.graphql.types.checkin.CheckInAttemptQl
import de.bixilon.unithen.api.graphql.types.user.CourseUserQl
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.util.progress.CourseFetchProgress
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Clock
import kotlin.uuid.Uuid

object CourseFetcher {
    const val MAX_PARALLEL_REQUESTS = 6

    private suspend fun SqlStorage.fetchCourse(account: Account, id: Uuid, semaphore: Semaphore, tutor: Boolean) {
        val site = sites[account.site]!!
        val api = account.api(site)

        val detailsQl = semaphore.withPermit { api.getCourse(id) }!!

        val course = storeCourse(site, detailsQl)
        accounts.addToCourse(account, course, tutor)

        if (tutor) {
            val enrolled = semaphore.withPermit { api.getEnrolled(course.uuid) }
            storeEnrolled(site, course, enrolled!!)
        }
    }


    suspend fun SqlStorage.updateCourses(account: Account, force: Boolean, progress: ((CourseFetchProgress) -> Unit)? = null) {
        val site = sites[account.site]!!
        val api = account.api(site)
        if (!force && !account.isStale()) return

        // TODO: do both requests in parallel
        val enrolled = api.getCourses(account.uuid, isEnrolled = true, isTutor = false)?.map { it.id }?.toSet() ?: emptySet()
        val tutor = api.getCourses(account.uuid, isEnrolled = false, isTutor = true)?.map { it.id }?.toSet() ?: emptySet()

        val all = enrolled + tutor

        progress?.invoke(CourseFetchProgress(0, all.size))

        val semaphore = Semaphore(MAX_PARALLEL_REQUESTS)

        setCourses(account, site, all, tutor)

        var done = 0
        var total = all.size

        coroutineScope {
            all.mapNotNull { id ->
                val course = this@updateCourses.courses[site, id]

                if (course != null && !course.isDataStale()) {
                    total--
                    progress?.invoke(CourseFetchProgress(done, total))
                    return@mapNotNull null
                }

                async(Dispatchers.IO) {
                    fetchCourse(account, id, semaphore, id in tutor)
                    progress?.invoke(CourseFetchProgress(done++, total))
                }
            }.awaitAll()
        }


        accounts.update(account.id, fetched = Clock.System.now())
    }

    suspend fun SqlStorage.updateCourse(account: Account, course: Course) {
        val site = sites[account.site]!!
        val api = account.api(site)

        if (!course.isDataStale()) return

        val detailsQl = api.getCourse(course.uuid)!!
        storeCourse(site, detailsQl)

        if (accounts.isTutor(account, course)) {
            val enrolled = api.getEnrolled(course.uuid)
            storeEnrolled(site, course, enrolled!!)
        }
    }

    private fun SqlStorage.setCourses(account: Account, site: Site, ids: Set<Uuid>, tutor: Set<Uuid>) {
        transaction {
            accounts.clearCourses(account)

            for (id in ids) {
                val course = courses[site, id] ?: continue
                accounts.addToCourse(account, course, id in tutor)
            }
        }
        cleanup()
    }


    private fun SqlStorage.storeCourse(site: Site, courseQl: CourseQl) = transaction {
        if (courseQl.name == null) throw NullPointerException("Course details not fetched, wrong query?")
        val evenQl = courseQl.event!!

        val event = events.add(site, evenQl.id, evenQl.name, evenQl.start, evenQl.end)


        val course = this.courses.add(site, event, courseQl.id, courseQl.name, Clock.System.now())

        courses.clearTutors(course)
        for (tutorQl in courseQl.tutors!!) {
            val tutor = users.add(site, tutorQl.id, tutorQl.firstname!!, tutorQl.lastname!!)
            courses.addTutor(tutor, course)
        }

        if (courseQl.appointments != null) {
            // TODO: Remove all other ones?
            for (appointmentQl in courseQl.appointments) {
                val appointment = appointments.add(course, appointmentQl.id, appointmentQl.start!!, appointmentQl.end!!, appointmentQl.canceledAt, appointmentQl.location!!.name)

                appointments.clearTutors(appointment)
                for (tutorQl in appointmentQl.tutors!!) {
                    val tutor = users.add(site, tutorQl.id, tutorQl.firstname!!, tutorQl.lastname!!)
                    appointments.addTutor(tutor, appointment)
                }
            }
        }


        courses.update(course.id, fetched = Clock.System.now())

        return@transaction course
    }


    private fun SqlStorage.storeEnrolled(site: Site, course: Course, enrolled: List<CourseUserQl>) = transaction {
        courses.clearEnrolled(course)
        for (enrolledQl in enrolled) {
            val enrolled = users.add(site, enrolledQl.id, enrolledQl.firstname!!, enrolledQl.lastname!!)

            courses.addEnrolled(enrolled, course)
        }
        courses.update(course.id, fetchedEnrolled = Clock.System.now())
    }

    private fun SqlStorage.storeAttendees(site: Site, appointment: Appointment, attendees: List<CourseUserQl>, attempts: List<CheckInAttemptQl>) = transaction {
        appointments.clearAttendees(appointment)
        checkInQueue.clearPendingCheckout(appointment)

        for (userQl in attendees) {
            val user = users.add(site, userQl.id, userQl.firstname!!, userQl.lastname!!)
            checkInQueue.delete(appointment, user)

            val attempt = attempts.find { it.status == CheckInAttemptQl.Status.SUCCESS && it.user?.id == userQl.id } ?: continue

            appointments.addAttendee(user, appointment, attempt.id)
        }
        appointments.update(appointment.id, fetchedAttendees = Clock.System.now())
    }


    suspend fun SqlStorage.fetchEnrolled(account: Account, course: Course, force: Boolean) {
        if (!force && !course.isEnrolledStale()) return

        val site = sites[account.site]!!
        val api = account.api(site)


        val enrolled = api.getEnrolled(course.uuid)

        storeEnrolled(site, course, enrolled!!)
    }

    suspend fun SqlStorage.fetchAttendees(account: Account, appointment: Appointment, force: Boolean) {
        val site = sites[account.site]!!
        val api = account.api(site)

        if (!force && !appointment.isAttendeesStale()) return

        val attemptsQl = api.getCheckInAttempts(appointment.uuid) ?: return
        storeAttendees(site, appointment, attemptsQl.attendees!!, attemptsQl.checkInAttempts!!)
    }
}
