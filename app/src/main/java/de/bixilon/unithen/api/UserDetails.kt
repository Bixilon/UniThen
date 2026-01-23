package de.bixilon.unithen.api

import java.util.*

data class UserDetails(
    val uuid: UUID,
    val firstname: String,
    val lastname: String,
    val email: String,
)
