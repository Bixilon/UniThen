package de.bixilon.unithen.api.authentication

import android.net.http.HttpEngine

class CookieAuthentication(
    val session: String,
) : Authentication {

    override fun authenticate(request: HttpEngine.Builder) {
       // ory_kratos_continuity=
        //    ory-session=
    }
}
