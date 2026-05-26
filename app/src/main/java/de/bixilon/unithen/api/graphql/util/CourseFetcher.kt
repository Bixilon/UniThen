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

import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.api.graphql.types.resource.CourseQl
import de.bixilon.unithen.api.graphql.types.user.CourseUserQl
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Site
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

object CourseFetcher {
    val COURSE_FETCH_INTERVAL = 1.hours


    fun SqlStorage.fetch(account: Account) {
        val now = Clock.System.now()
        val site = sites[account.site]!!
        val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(account.session ?: ""))
        val coursesQl = api.getCourses(account.uuid) ?: throw NullPointerException("Could not fetch course overview?")

        for (courseQl in coursesQl) {
            var course = this.courses[site, courseQl.id]

            if (course != null && (now - course.fetched) < COURSE_FETCH_INTERVAL) {
                accounts.addToCourse(account, course)
                continue
            }


            val detailsQl = api.getCourse(courseQl.id)!!

            course = store(site, detailsQl)

            if (detailsQl.tutors?.any { account.uuid == it } ?: false) { // TODO: is that the way to check?
                val enrolled = api.getEnrolled(course.uuid)
                store(site, course, enrolled!!)
            }

            accounts.addToCourse(account, course)
        }


        accounts.update(account.id, fetched = Clock.System.now())
    }


    private fun SqlStorage.store(site: Site, courseQl: CourseQl) = transaction {
        if (courseQl.name == null) throw NullPointerException("Course details not fetched, wrong query?")
        val evenQl = courseQl.event!!

        val event = events.add(site, evenQl.id, evenQl.name, evenQl.start, evenQl.end)


        val course = this.courses.add(site, event, courseQl.id, courseQl.name, Clock.System.now())

        courses.clearTutors(course)
        for (tutorQl in courseQl.tutors!!) {
            val tutor = users.add(site, tutorQl.id, tutorQl.firstName!!, tutorQl.lastName!!)
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
        for (enrolledQl in enrolled) {
            val enrolled = users.add(site, enrolledQl.id, enrolledQl.firstName!!, enrolledQl.lastName!!)

            courses.addEnrolled(enrolled, course)
        }
        // TODO: remove all others
    }
}
