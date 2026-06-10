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

import de.bixilon.unithen.api.graphql.types.checkin.CheckInAttemptQl
import de.bixilon.unithen.api.graphql.types.CourseQl
import de.bixilon.unithen.api.graphql.types.user.CourseUserQl
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.Site
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Clock

object CourseFetcher {
    const val MAX_PARALLEL_REQUESTS = 6


    suspend fun SqlStorage.fetch(account: Account, force: Boolean) {
        val site = sites[account.site]!!
        val api = account.api(site)
        if (!force && !account.isStale()) return

        val coursesQl = api.getCourses(account.uuid) ?: throw NullPointerException("Could not fetch course overview?")

        val semaphore = Semaphore(MAX_PARALLEL_REQUESTS)

        transaction {
            accounts.clearCourses(account)

            for (courseQl in coursesQl) {
                val course = courses[site, courseQl.id] ?: continue
                accounts.addToCourse(account, course)
            }
        }

        coroutineScope {
            coursesQl.mapNotNull { courseQl ->
                val course = this@fetch.courses[site, courseQl.id]

                if (course != null && !course.isDataStale()) return@mapNotNull null

                async {
                    val detailsQl = semaphore.withPermit { api.getCourse(courseQl.id)!! }

                    val course = store(site, detailsQl)

                    if (detailsQl.tutors?.any { account.uuid == it.id } ?: false) { // TODO: is that the way to check?
                        val enrolled = semaphore.withPermit { api.getEnrolled(course.uuid) }
                        store(site, course, enrolled!!)
                    }

                    accounts.addToCourse(account, course)
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


        if (detailsQl.tutors?.any { account.uuid == it.id } ?: false) { // TODO: is that the way to check?
            val enrolled = api.getEnrolled(course.uuid)
            store(site, course, enrolled!!)
        }

        accounts.addToCourse(account, course)
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

        for (appointmentQl in courseQl.appointments!!) {
            val appointment = appointments.add(course, appointmentQl.id, appointmentQl.start!!, appointmentQl.end!!, appointmentQl.canceledAt, appointmentQl.location!!.name)

            appointments.clearTutors(appointment)
            for (tutorQl in appointmentQl.tutors!!) {
                val user = users[site, tutorQl.id] ?: continue // TODO: Warn if tutor is not in course->tutors?
                appointments.addTutor(user, appointment)
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
