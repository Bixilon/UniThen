package de.bixilon.unithen.api.graphql.types

import de.bixilon.unithen.api.graphql.types.location.Location
import java.util.Date
import java.util.UUID

data class Appointment(
    override val id: UUID,
    val start: Date,
    val end: Date,
    val cancelledAt: Date?,
    val tutors: List<Tutor>,
    val location: Location,
): Identified
