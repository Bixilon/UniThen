package de.bixilon.unithen.api

import de.bixilon.unithen.api.authentication.Authentication
import okhttp3.Request
import java.net.URI
import java.util.*

open class AuthenticatedUniNowApi(
    url: URI,
    val userId: UUID,
    val authentication: Authentication,
) : UniNowApi(url) {

    override fun buildRequest(endpoint: String): Request.Builder {
        val request = super.buildRequest(endpoint)

        authentication.authenticate(request)

        return request
    }
}
