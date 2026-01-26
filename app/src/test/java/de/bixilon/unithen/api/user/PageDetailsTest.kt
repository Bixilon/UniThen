package de.bixilon.unithen.api.user

import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.kutil.uri.URIUtil.toURI
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PageDetailsTest {

    // @Test
    fun `fetch zhs`() {
        val details = SiteDetails.fetch("https://kurse.zhs-muenchen.de".toURI())
        assertEquals(details.name, "ZHS München")
    }

    @Test
    fun `parse zhs`() {
        val html = UserDetailsTest::class.java.getResourceAsStream("/http/zhs_front_page.html")!!.readAsString()
        val array = ByteArray(0)

        val details = SiteDetails.parse(html) { if (it == "https://kurse.zhs-muenchen.de/services/image-proxy/rs:fit:192:192:1/plain/https://uninow-campus365-staging.s3.sbg.io.cloud.ovh.net/settings.management/kdamysccpykixszkuxtoorvcjgigcnba.png".toURI()) array else Broken() }
        assertEquals(details.name, "ZHS München")
        assert(details.icon === array)
    }

    @Test
    fun `parse aaa`() {
        val html = UserDetailsTest::class.java.getResourceAsStream("/http/aaa_front_page.html")!!.readAsString()
        val array = ByteArray(0)

        val details = SiteDetails.parse(html) { if (it == "https://aaa-giessen.uninow.com/services/image-proxy/rs:fit:192:192:1/plain/https://uninow-campus365-staging.s3.sbg.io.cloud.ovh.net/settings.management/ypsuldntspdqannpuneuiyvuyhbjumsv.png".toURI()) array else Broken() }
        assertEquals(details.name, "Deutschkurse Buchungsplattform")
        assert(details.icon === array)
    }

    @Test
    fun `fix url correct`() {
        val url = SiteDetails.fix("https://kurse.zhs-muenchen.de/")

        assertEquals(url, "https://kurse.zhs-muenchen.de".toURI())
    }

    @Test
    fun `fix url no scheme`() {
        val url = SiteDetails.fix("kurse.zhs-muenchen.de/")

        assertEquals(url, "https://kurse.zhs-muenchen.de".toURI())
    }

    @Test
    fun `fix url with path`() {
        val url = SiteDetails.fix("kurse.zhs-muenchen.de/de")

        assertEquals(url, "https://kurse.zhs-muenchen.de".toURI())
    }
}
