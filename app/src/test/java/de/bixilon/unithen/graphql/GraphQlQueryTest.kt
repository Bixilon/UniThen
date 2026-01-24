package de.bixilon.unithen.graphql

import com.fasterxml.jackson.module.kotlin.readValue
import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.unithen.api.graphql.UserPkPostings
import de.bixilon.unithen.api.graphql.types.Course
import de.bixilon.unithen.api.graphql.types.Posting
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
        val response = readResponse<Posting>("posting")

        assertEquals(response.id, "b2583378-fbdd-48ab-81c6-ab3ddfb0236c".toUUID())

        val course = response.product.resource.cast<Course>()

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
