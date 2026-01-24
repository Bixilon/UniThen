package de.bixilon.unithen.api.graphql.types

import java.util.*

const val COURSE_TYPE = "Course"

data class CourseQl(
    override val id: UUID,
    val name: String,
    val event: EventQl,
    val tutors: List<TutorQl>,
    val appointments: List<AppointmentQl>,
) : ResourceQl {
    override val __typename get() = COURSE_TYPE
}
