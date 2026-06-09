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

package de.bixilon.unithen.graphql

import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.unithen.api.graphql.queries.Mutations
import de.bixilon.unithen.api.graphql.queries.Queries
import de.bixilon.unithen.api.graphql.types.PostingQl
import de.bixilon.unithen.api.graphql.types.checkin.CheckInAttemptQl
import de.bixilon.unithen.api.graphql.types.resource.CourseQl
import de.bixilon.unithen.api.graphql.types.user.CourseUserQl
import de.bixilon.unithen.util.Jackson
import de.bixilon.unithen.util.Kutil.toUuid
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import java.io.FileNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphQlQueryTest {

    private inline fun <reified T> readResponse(name: String): T {
        val resource = GraphQlQueryTest::class.java.getResourceAsStream("/graphql/$name.json") ?: throw FileNotFoundException("Can not find resource $name")


        return Jackson.GRAPHQL.decodeFromString<T>(resource.readAsString())
    }

    @Test
    fun `read courses slim`() {
        val response = readResponse<Queries>("courses")

        assertEquals(response.userPk!!.postings!!.size, 2)
    }

    @Test
    fun `read posting`() {
        val response = readResponse<PostingQl>("posting")

        assertEquals(response.id, "b2583378-fbdd-48ab-81c6-ab3ddfb0236c".toUuid())

        val course = response.product.resource.cast<CourseQl>()

        assertEquals(course.name, "Yoga - Allround Yoga - Yogilates - Meditation")

        val appointment = course.appointments!!.first()

        assertEquals(appointment.id, "a648c0a1-aa4a-4484-a888-69aded7db109".toUuid())
        assertEquals(appointment.start, LocalDateTime(2026, Month.MARCH, 11, 17, 30, 0).toInstant(UtcOffset.ZERO))
    }

    @Test
    fun `read courses response`() {
        val response = readResponse<Queries>("courses_response")

        assertEquals(response.userPk!!.postings!!.size, 1)
    }

    @Test
    fun `read course response`() {
        val response = readResponse<Queries>("course")

        assertEquals(response.course?.name, "AcroYoga - Open Level")
        assertEquals(response.course?.tutors, listOf(
            CourseUserQl("dadbcf91-7a20-41dc-a9e8-9f591395531e".toUuid(), "First tutor", "Lastname"),
            CourseUserQl("4487d6ed-6947-405a-8404-334d65dd823e".toUuid(), "Second", "Tutor"),
        ))
    }

    @Test
    fun `read attempts response`() {
        val response = readResponse<Queries>("attempts")


        assertEquals(response.appointment?.attendees, listOf(
            CourseUserQl("3b451d02-2fc8-4bed-b8c5-50fb91280f30".toUuid(), "First tutor", "Lastname"),
        ))
        assertEquals(response.appointment?.checkInAttempts, listOf(
            CheckInAttemptQl("39b4858a-cb3a-42ff-9b6e-e6a4d54c2de2".toUuid(), CheckInAttemptQl.Status.ERROR, user = CourseUserQl("3b451d02-2fc8-4bed-b8c5-50fb91280f30".toUuid())),
        ))
    }

    @Test
    fun `read checkin response`() {
        val response = readResponse<Mutations>("checkin")

        assertEquals(response.appointmentCheckin?.status, CheckInAttemptQl.Status.SUCCESS)
    }

    @Test
    fun `read delete checkin response`() {
        val response = readResponse<Mutations>("delete_checkin")

        assertEquals(response.deleteCheckinAttempt?.status, CheckInAttemptQl.Status.SUCCESS)
    }

    @Test
    fun `read course with unknown tutor`() {
        // That is the most dumb thing I have ever seen.
        val response = readResponse<Queries>("course_unknown_tutor")

        assertEquals(response.course?.name, "Golf - Freies Spiel")
        assertEquals(response.course?.tutors, listOf(
            CourseUserQl("4487d6ed-6947-405a-8404-334d65dd823e".toUuid(), "Second", "Tutor"),
        ))
    }
}
