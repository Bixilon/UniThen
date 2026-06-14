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

import de.bixilon.unithen.api.graphql.types.AppointmentQl
import de.bixilon.unithen.api.graphql.types.CourseQl
import de.bixilon.unithen.api.graphql.types.checkin.CheckInAttemptQl
import de.bixilon.unithen.api.graphql.types.user.CourseUserQl
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.util.progress.CourseFetchProgress
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Clock
import kotlin.uuid.Uuid

object CourseFetcher {
    const val MAX_PARALLEL_REQUESTS = 6


    private fun CourseQl.isTutor(account: Account): Boolean {
        if (tutors == null) throw NullPointerException("Tutors not fetched!")
        val tutors = tutors.toMutableSet()

        appointments?.let {
            for (appointment in appointments) {
                tutors += appointment.tutors ?: continue
            }
        }

        return tutors.any { account.uuid == it.id }
    }

    private suspend fun SqlStorage.fetchCourse(account: Account, id: Uuid, slim: Boolean, semaphore: Semaphore, appointments: List<AppointmentQl>?) {
        val site = sites[account.site]!!
        val api = account.api(site)

        val detailsQl = semaphore.withPermit { if (slim) api.getCourseSlim(id) else api.getCourse(id) }!!.let { it.copy(appointments = appointments ?: it.appointments) }

        val course = store(site, detailsQl)
        accounts.addToCourse(account, course)

        if (detailsQl.isTutor(account)) {
            val enrolled = semaphore.withPermit { api.getEnrolled(course.uuid) }
            store(site, course, enrolled!!)
        }
    }


    suspend fun SqlStorage.fetchFromCourses(account: Account, force: Boolean, progress: ((CourseFetchProgress) -> Unit)? = null) {
        val site = sites[account.site]!!
        val api = account.api(site)
        if (!force && !account.isStale()) return

        val coursesQl = api.getCourses(account.uuid) ?: throw NullPointerException("No courses?")

        progress?.invoke(CourseFetchProgress(0, coursesQl.size))

        val semaphore = Semaphore(MAX_PARALLEL_REQUESTS)

        setCourses(account, site, coursesQl.map { it.id }.toSet())

        var done = 0
        var total = coursesQl.size

        coroutineScope {
            coursesQl.mapNotNull { courseQl ->
                val course = this@fetchFromCourses.courses[site, courseQl.id]

                if (course != null && !course.isDataStale()) {
                    total--
                    progress?.invoke(CourseFetchProgress(done, total))
                    return@mapNotNull null
                }

                async {
                    fetchCourse(account, courseQl.id, false, semaphore, null)
                    progress?.invoke(CourseFetchProgress(done++, total))
                }
            }.awaitAll()
        }


        accounts.update(account.id, fetched = Clock.System.now())
    }

    suspend fun SqlStorage.fetchFromAppointments(account: Account, force: Boolean, progress: ((CourseFetchProgress) -> Unit)? = null) {
        val site = sites[account.site]!!
        val api = account.api(site)
        if (!force && !account.isStale()) return

        val appointmentsQl = api.getAppointments() ?: throw NullPointerException("No appointments?")
        val coursesIds = appointmentsQl.map { it.course!!.id }.toSet()

        progress?.invoke(CourseFetchProgress(0, coursesIds.size))

        val semaphore = Semaphore(MAX_PARALLEL_REQUESTS)

        setCourses(account, site, coursesIds)

        var done = 0
        var total = coursesIds.size

        coroutineScope {
            coursesIds.mapNotNull { courseId ->
                val course = this@fetchFromAppointments.courses[site, courseId]

                if (course != null && !course.isDataStale()) {
                    total--
                    progress?.invoke(CourseFetchProgress(done, total))
                    return@mapNotNull null
                }

                async {
                    val appointments = appointmentsQl.filter { it.course!!.id == courseId }
                    fetchCourse(account, courseId, true, semaphore, appointments)

                    progress?.invoke(CourseFetchProgress(done++, total))
                }
            }.awaitAll()
        }


        accounts.update(account.id, fetched = Clock.System.now())
    }

    suspend fun SqlStorage.fetch(account: Account, course: Course) {
        val site = sites[account.site]!!
        val api = account.api(site)

        if (!course.isDataStale()) return

        val detailsQl = api.getCourse(course.uuid)!!
        store(site, detailsQl)
        accounts.addToCourse(account, course)


        if (detailsQl.isTutor(account)) {
            val enrolled = api.getEnrolled(course.uuid)
            store(site, course, enrolled!!)
        }
    }

    private fun SqlStorage.setCourses(account: Account, site: Site, ids: Set<Uuid>) {
        transaction {
            accounts.clearCourses(account)

            for (id in ids) {
                val course = courses[site, id] ?: continue
                accounts.addToCourse(account, course)
            }
        }
        cleanup()
    }


    private fun SqlStorage.store(site: Site, courseQl: CourseQl) = transaction {
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


    private fun SqlStorage.store(site: Site, course: Course, enrolled: List<CourseUserQl>) = transaction {
        courses.clearEnrolled(course)
        for (enrolledQl in enrolled) {
            val enrolled = users.add(site, enrolledQl.id, enrolledQl.firstname!!, enrolledQl.lastname!!)

            courses.addEnrolled(enrolled, course)
        }
        courses.update(course.id, fetchedEnrolled = Clock.System.now())
    }

    private fun SqlStorage.store(site: Site, appointment: Appointment, attendees: List<CourseUserQl>, attempts: List<CheckInAttemptQl>) = transaction {
        appointments.clearAttendees(appointment)
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

        store(site, course, enrolled!!)
    }

    suspend fun SqlStorage.fetchAttendees(account: Account, appointment: Appointment, force: Boolean) {
        val site = sites[account.site]!!
        val api = account.api(site)

        if (!force && !appointment.isAttendeesStale()) return

        val attemptsQl = api.getCheckInAttempts(appointment.uuid) ?: return
        store(site, appointment, attemptsQl.attendees!!, attemptsQl.checkInAttempts!!)
    }
}
