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

import com.fasterxml.jackson.module.kotlin.readValue
import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.unithen.api.graphql.UserPkPostings
import de.bixilon.unithen.api.graphql.types.CourseQl
import de.bixilon.unithen.api.graphql.types.PostingQl
import de.bixilon.unithen.util.Jackson
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.time.Month

class GraphQlQueryTest {

    private inline fun <reified T> readResponse(name: String): T {
        val resource = GraphQlQueryTest::class.java.getResourceAsStream("/graphql/$name.json") ?: throw FileNotFoundException("Can not find resource $name")


        return Jackson.GRAPH_QL.readValue<T>(resource)
    }

    @Test
    fun `read posting`() {
        val response = readResponse<PostingQl>("posting")

        assertEquals(response.id, "b2583378-fbdd-48ab-81c6-ab3ddfb0236c".toUUID())

        val course = response.product.resource.cast<CourseQl>()

        assertEquals(course.name, "Yoga - Allround Yoga - Yogilates - Meditation")

        val appointment = course.appointments.first()

        assertEquals(appointment.id, "a648c0a1-aa4a-4484-a888-69aded7db109".toUUID())
        assertEquals(appointment.start, LocalDateTime.of(2026, Month.MARCH, 11, 17, 30, 0))
    }

    @Test
    fun `read courses response`() {
        val response = readResponse<UserPkPostings>("courses_response")

        assertEquals(response.userPk.postings.size, 1)
    }
}
