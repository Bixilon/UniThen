package de.bixilon.unithen.api.graphql.types

import java.time.LocalDateTime
import java.util.*

data class EventQl(
    override val id: UUID,
    val name: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
) : IdentifiedQl
