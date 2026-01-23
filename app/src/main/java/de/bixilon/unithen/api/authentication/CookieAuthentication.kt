package de.bixilon.unithen.api.authentication

import okhttp3.Request

class CookieAuthentication(
    val session: String,
) : Authentication {

    override fun authenticate(request: Request.Builder) {
        request.header("Cookie", "ory-session=$session")
    }
}
