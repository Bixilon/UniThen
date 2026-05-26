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

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.api.graphql.types.resource.CourseQl
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Site
import kotlin.time.Clock

object CourseFetcher {

    fun SqlStorage.fetch(account: Account) {
        val site = sites[account.site]!!
        val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(account.session ?: ""))
        val postings = api.getPostings(account.uuid) ?: throw NullPointerException("Could not fetch postings?")

        for (postingQl in postings) {
            val courseQl = postingQl.product.resource.nullCast<CourseQl>() ?: continue

            val course = store(site, courseQl)

            accounts.addToCourse(account, course)
        }


        accounts.update(account.id, fetched = Clock.System.now())
    }


    private fun SqlStorage.store(site: Site, courseQl: CourseQl) = transaction {
        val evenQl = courseQl.event

        val event = events.add(site, evenQl.id, evenQl.name, evenQl.start, evenQl.end)


        val course = this.courses.add(site, event, courseQl.id, courseQl.name, Clock.System.now())

        courses.clearTutors(course)
        for (tutorQl in courseQl.tutors) {
            val tutor = users.add(site, tutorQl.id, tutorQl.firstName, tutorQl.lastName)
            courses.addTutor(tutor, course)
        }

        for (appointmentQl in courseQl.appointments) {
            val appointment = appointments.add(course, appointmentQl.id, appointmentQl.start, appointmentQl.end, appointmentQl.canceledAt, appointmentQl.location.name)

            appointments.clearTutors(appointment)
            for (tutorQl in appointmentQl.tutors) {
                val user = users[site, tutorQl.id] ?: continue // TODO: Warn if tutor is not in course->tutors?
                appointments.addTutor(user, appointment)
            }
        }


        courses.update(course.id, fetched = Clock.System.now())

        return@transaction course
    }
}
