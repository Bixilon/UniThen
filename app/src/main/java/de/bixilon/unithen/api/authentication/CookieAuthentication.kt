package de.bixilon.unithen.api.authentication

import okhttp3.Request

data class CookieAuthentication(
    val session: String,
) : Authentication {

    override fun authenticate(request: Request.Builder) {
        request.header("Cookie", "ory-session=$session")
    }
}
