package de.bixilon.unithen.storage

import java.util.*

data class Account(
    override val id: Key,
    val site: Key,
    val uuid: UUID,

    val firstname: String,
    val lastname: String,

    @Deprecated("multiple authentication methods")
    val session: String,
) : DbKeyed
