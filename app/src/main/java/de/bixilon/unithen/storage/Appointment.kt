package de.bixilon.unithen.storage

import java.time.LocalDateTime
import java.util.*

data class Appointment(
    override val id: Key,
    val course: Key,
    val uuid: UUID,
    val start: LocalDateTime,
    val end: LocalDateTime,
) : DbKeyed
