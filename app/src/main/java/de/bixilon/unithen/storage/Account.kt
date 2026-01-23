package de.bixilon.unithen.storage

import de.bixilon.unithen.api.authentication.CookieAuthentication
import java.util.*

data class Account(
    val id: Int,
    val pk: UUID,
    val sessionKey: String,
) {

    fun createAuthentication() = CookieAuthentication(sessionKey)
}
