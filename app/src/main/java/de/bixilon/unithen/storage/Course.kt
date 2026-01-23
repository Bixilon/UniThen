package de.bixilon.unithen.storage

import java.util.*

data class Course(
    override val id: Key,
    val site: Key,
    val uuid: UUID,
    val name: String,
) : DbKeyed
