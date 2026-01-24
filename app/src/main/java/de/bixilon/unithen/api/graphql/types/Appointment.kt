package de.bixilon.unithen.api.graphql.types

import de.bixilon.unithen.api.graphql.types.location.Location
import java.time.LocalDateTime
import java.util.*

data class Appointment(
    override val id: UUID,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val cancelledAt: LocalDateTime?,
    val tutors: List<Tutor>,
    val location: Location,
): Identified
