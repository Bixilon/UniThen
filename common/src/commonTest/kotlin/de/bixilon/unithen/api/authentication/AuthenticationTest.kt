package de.bixilon.unithen.api.authentication

import kotlin.test.Test
import kotlin.test.assertEquals

class AuthenticationTest {

    @Test
    fun `legacy cookie`() {
        val authentication = Authentication.of("something")

        assertEquals(authentication, CookieAuthentication("something"))
    }

    @Test
    fun `cookie json`() {
        val authentication = Authentication.of("""{"type":"cookie","token": "something"}""")

        assertEquals(authentication, CookieAuthentication("something"))
    }

    @Test
    fun `ory json`() {
        val authentication = Authentication.of("""{"type":"ory","token": "ory_abc"}""")

        assertEquals(authentication, OryTokenAuthentication("ory_abc"))
    }
}
