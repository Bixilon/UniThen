package de.bixilon.unithen.api.user

import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.kutil.uri.URIUtil.toURI
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.unithen.api.authentication.CookieAuthentication
import junit.framework.TestCase.assertEquals
import org.junit.Test

class UserDetailsTest {

    // @Test
    fun `fetch zhs`() {
        val authentication = CookieAuthentication("XXX")


        val details = UserDetails.fetch("https://kurse.zhs-muenchen.de".toURI(), authentication)
        assertEquals(details.uuid, "10000000-0003-0000-0000-000000000001".toUUID())
    }

    @Test
    fun `parse zhs`() {
        val html = UserDetailsTest::class.java.getResourceAsStream("/http/zhs_front_page.html")!!.readAsString()
        val expected = UserDetails("10000000-0003-0000-0000-000000000001".toUUID(), "Max", "Muster", "mail@server.de")

        val details = UserDetails.parse(html)
        assertEquals(details, expected)
    }
}
