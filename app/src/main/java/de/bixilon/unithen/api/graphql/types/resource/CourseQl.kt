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

package de.bixilon.unithen.api.graphql.types.resource

import de.bixilon.unithen.api.graphql.types.AppointmentQl
import de.bixilon.unithen.api.graphql.types.EventQl
import de.bixilon.unithen.api.graphql.types.user.CourseUserQl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

const val COURSE_TYPE = "Course"

@Serializable
@SerialName(COURSE_TYPE)
data class CourseQl(
    override val id: Uuid,
    val name: String? = null,
    val event: EventQl? = null,
    val tutors: List<CourseUserQl>? = null,
    val appointments: List<AppointmentQl>? = null,
    val enrolled: List<CourseUserQl>? = null,
) : ResourceQl {
    override val __typename get() = COURSE_TYPE
}
