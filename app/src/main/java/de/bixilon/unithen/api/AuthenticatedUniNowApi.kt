package de.bixilon.unithen.api

import de.bixilon.unithen.api.authentication.Authentication
import java.net.URL
import java.util.UUID

open class AuthenticatedUniNowApi(
    url: URL,
    val userId: UUID,
    val authentication: Authentication?,
) : UniNowApi(url) {

}
