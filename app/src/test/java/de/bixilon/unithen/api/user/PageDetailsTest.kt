/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen.api.user

import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.kutil.uri.URIUtil.toURI
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `remove https`() {
        val url = SiteDetails.fix("https://")

        assertEquals(url, "")
    }

    @Test
    fun `fix url correct`() {
        val url = SiteDetails.fix("https://kurse.zhs-muenchen.de/")

        assertEquals(url, "kurse.zhs-muenchen.de")
    }

    @Test
    fun `fix url no scheme`() {
        val url = SiteDetails.fix("kurse.zhs-muenchen.de/")

        assertEquals(url, "kurse.zhs-muenchen.de")
    }

    @Test
    fun `fix url with path`() {
        val url = SiteDetails.fix("kurse.zhs-muenchen.de/de")

        assertEquals(url, "kurse.zhs-muenchen.de")
    }
}
