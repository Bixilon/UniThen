package de.bixilon.unithen.api

import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.kutil.uri.URIUtil.toURI
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.unithen.api.authentication.CookieAuthentication
import junit.framework.TestCase.assertEquals
import org.junit.Test


class UniNowUtilTest {

    // @Test
    fun `get user id`() {
        val authentication = CookieAuthentication("XXX")


        val userId = UniNowUtil.fetchUserId( "https://kurse.zhs-muenchen.de".toURI(), authentication)
        assertEquals(userId, "10000000-0003-0000-0000-000000000001".toUUID())
    }
    @Test
    fun `extract userid from frontpage`() {
        val html = UniNowUtilTest::class.java.getResourceAsStream("/http/front_page.html")!!.readAsString()

        val userId = UniNowUtil.extractUserId(html)
        assertEquals(userId, "10000000-0003-0000-0000-000000000001".toUUID())
    }
}
