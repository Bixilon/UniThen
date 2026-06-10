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

package de.bixilon.unithen.api.graphql.queries

import de.bixilon.unithen.api.graphql.types.AppointmentQl
import de.bixilon.unithen.api.graphql.types.CourseQl
import de.bixilon.unithen.api.graphql.types.user.UserQl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Queries(
    @SerialName("user_pk") val userPk: UserQl?,
    @SerialName("appointments_pk") val appointment: AppointmentQl?,
    @SerialName("appointments") val appointments: List<AppointmentQl>?,

    @SerialName("courses_pk") val course: CourseQl?,
)
