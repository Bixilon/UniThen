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

package de.bixilon.unithen.api

import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.graphql.queries.Mutations
import de.bixilon.unithen.api.graphql.queries.Queries
import de.bixilon.unithen.api.graphql.types.AppointmentQl
import de.bixilon.unithen.api.graphql.types.CourseQl
import de.bixilon.unithen.api.graphql.types.checkin.CheckInAttemptQl
import de.bixilon.unithen.api.graphql.types.user.CourseUserQl
import de.bixilon.unithen.api.ory.Whoami
import de.bixilon.unithen.api.user.UserDetails
import de.bixilon.unithen.ui.error.SerializationExceptionData
import de.bixilon.unithen.util.Jackson
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import kotlin.uuid.Uuid

open class AuthenticatedUniNowApi(
    host: String,
    val authentication: Authentication,
) : UniNowApi(host) {

    override suspend fun buildRequest(endpoint: String): HttpRequestBuilder {
        val request = super.buildRequest(endpoint)

        authentication.authenticate(request)

        return request
    }

    suspend fun getCourses(user: Uuid): List<CourseQl>? {
        return graphql<Queries>("courses", "user" to JsonPrimitive(user.toString())).userPk?.courses
    }

    suspend fun getAppointments(): List<AppointmentQl>? {
        return graphql<Queries>("appointments").appointments
    }

    suspend fun getCourse(course: Uuid): CourseQl? {
        return graphql<Queries>("course", "course" to JsonPrimitive(course.toString())).course
    }

    suspend fun getCourseSlim(course: Uuid): CourseQl? {
        return graphql<Queries>("course_slim", "course" to JsonPrimitive(course.toString())).course
    }

    suspend fun getEnrolled(course: Uuid): List<CourseUserQl>? {
        return graphql<Queries>("enrolled", "course" to JsonPrimitive(course.toString())).course?.enrolled
    }

    suspend fun getCheckInAttempts(appointment: Uuid): AppointmentQl? {
        return graphql<Queries>("attempts", "appointment" to JsonPrimitive(appointment.toString())).appointment
    }

    suspend fun checkInUser(appointment: Uuid, user: Uuid): CheckInAttemptQl? {
        return graphql<Mutations>("checkin", "appointment" to JsonPrimitive(appointment.toString()), "user" to JsonPrimitive(user.toString())).appointmentCheckin
    }

    suspend fun deleteCheckInAttempt(attempt: Uuid): CheckInAttemptQl? {
        return graphql<Mutations>("delete_checkin", "attempt" to JsonPrimitive(attempt.toString())).deleteCheckinAttempt
    }

    suspend fun getUserDetails(): UserDetails {
        val response = get("/services/identity/sessions/whoami")
        val whoami = try {
            Jackson.MAPPER.decodeFromString<Whoami>(response)
        } catch (error: SerializationException) {
            throw SerializationExceptionData(response, error)
        }

        return UserDetails(whoami.identity.id, whoami.identity.traits.name.first, whoami.identity.traits.name.last)
    }
}
