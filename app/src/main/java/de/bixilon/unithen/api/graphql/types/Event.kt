package de.bixilon.unithen.api.graphql.types

import java.util.Date
import java.util.UUID

data class Event(
    override val id: UUID,
    val name: String,
    val start: Date,
    val end: Date,
) : Identified
