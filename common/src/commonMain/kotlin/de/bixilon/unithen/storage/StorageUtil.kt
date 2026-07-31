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

package de.bixilon.unithen.storage

import de.bixilon.unithen.api.graphql.types.CourseQl
import de.bixilon.unithen.api.graphql.types.checkin.CheckInAttemptQl
import de.bixilon.unithen.api.graphql.types.user.CourseUserQl
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.Site
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

object StorageUtil {

    fun SqlStorage.storeCourse(site: Site, courseQl: CourseQl) = transaction {
        if (courseQl.name == null) throw NullPointerException("Course details not fetched, wrong query?")
        val eventQl = courseQl.event!!

        val event = events.add(site, eventQl.id, eventQl.name, eventQl.start, eventQl.end)


        val course = this.courses.add(site, event, courseQl.id, courseQl.name, Instant.DISTANT_PAST)

        courses.clearTutors(course)
        for (tutorQl in courseQl.tutors!!) {
            val tutor = users.add(site, tutorQl.id, tutorQl.firstname!!, tutorQl.lastname!!)
            courses.addTutor(tutor, course)
        }

        // TODO: Remove all other ones?
        for (appointmentQl in courseQl.appointments!!) {
            val appointment = appointments.add(course, appointmentQl.id, appointmentQl.start!!, appointmentQl.end!!, appointmentQl.canceledAt, appointmentQl.location!!.name)

            appointments.clearTutors(appointment)
            for (tutorQl in appointmentQl.tutors!!) {
                val tutor = users.add(site, tutorQl.id, tutorQl.firstname!!, tutorQl.lastname!!)
                appointments.addTutor(tutor, appointment)
            }
        }


        courses.update(course.id, fetched = Clock.System.now())

        return@transaction course
    }

    fun SqlStorage.storeEnrolled(site: Site, course: Course, enrolled: List<CourseUserQl>) = transaction {
        courses.clearEnrolled(course)
        for (enrolledQl in enrolled) {
            val enrolled = users.add(site, enrolledQl.id, enrolledQl.firstname!!, enrolledQl.lastname!!)

            courses.addEnrolled(enrolled, course)
        }
        courses.update(course.id, fetchedEnrolled = Clock.System.now())
    }

    fun SqlStorage.storeAttendees(site: Site, appointment: Appointment, attendees: List<CourseUserQl>, attempts: List<CheckInAttemptQl>) = transaction {
        appointments.clearAttendees(appointment)
        checkInQueue.clearPendingCheckout(appointment)

        for (userQl in attendees) {
            val user = users.add(site, userQl.id, userQl.firstname!!, userQl.lastname!!)
            checkInQueue.delete(appointment, user) // TODO: only if check in pending or errored

            val attempt = attempts.find { it.status == CheckInAttemptQl.Status.SUCCESS && it.user?.id == userQl.id } ?: continue

            appointments.addAttendee(user, appointment, attempt.id)
        }
        appointments.update(appointment.id, fetchedAttendees = Clock.System.now())
    }


    fun SqlStorage.setCourses(account: Account, site: Site, ids: Set<Uuid>, tutor: Set<Uuid>) {
        transaction {
            accounts.clearCourses(account)

            for (id in ids) {
                val course = courses[site, id] ?: continue
                accounts.addToCourse(account, course, id in tutor)
            }
        }
        cleanup()
    }
}
