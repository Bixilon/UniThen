package de.bixilon.unithen.api

import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.kutil.uri.URIUtil.toURI
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.api.user.UserDetails
import junit.framework.TestCase.assertEquals
import org.junit.Test


class UniNowUtilTest {

    // @Test
    fun `fetch user details`() {
        val authentication = CookieAuthentication("XXX")


        val userId = UniNowUtil.fetchUserDetails("https://kurse.zhs-muenchen.de".toURI(), authentication)
        assertEquals(userId.uuid, "10000000-0003-0000-0000-000000000001".toUUID())
    }

    @Test
    fun `extract user details from frontpage`() {
        val html = UniNowUtilTest::class.java.getResourceAsStream("/http/front_page.html")!!.readAsString()
        val expected = UserDetails("10000000-0003-0000-0000-000000000001".toUUID(), "Max", "Muster", "mail@server.de")

        val details = UniNowUtil.extractUserDetails(html)
        assertEquals(details, expected)
    }
}
