package de.bixilon.unithen.api.graphql.types

import de.bixilon.unithen.api.graphql.types.location.LocationQl
import java.time.LocalDateTime
import java.util.*

data class AppointmentQl(
    override val id: UUID,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val cancelledAt: LocalDateTime?,
    val tutors: List<TutorQl>,
    val location: LocationQl,
) : IdentifiedQl
