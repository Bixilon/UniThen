package de.bixilon.unithen.storage

import java.util.*

data class Appointment(
    override val id: Key,
    val course: Key,
    val uuid: UUID,
    val start: Date,
    val end: Date,
) : DbKeyed
