package de.bixilon.unithen.storage

import java.util.*

data class Appointment(
    val id: Int,
    val uuid: UUID,
    val start: Date,
    val end: Date,
)
