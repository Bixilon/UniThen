package de.bixilon.unithen.api.graphql.types

import java.util.UUID

const val COURSE_TYPE = "Course"

data class Course(
    override val id: UUID,
    val name: String,
    val event: Event,
    val tutors: List<Tutor>,
    val appointments: List<Appointment>,
): Resource {
    override val __typename get() = COURSE_TYPE
}
