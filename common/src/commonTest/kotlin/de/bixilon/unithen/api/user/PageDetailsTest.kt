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

import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.RuntimeInfo.RuntimeInfo0
import kotlinx.coroutines.runBlocking
import unithen.common.generated.resources.Res
import kotlin.test.Test
import kotlin.test.assertEquals

class PageDetailsTest {

    init {
        RuntimeInfo0.actual = object : RuntimeInfo {
            override val debug get() = false
        }
    }

    // @Test
    fun `fetch zhs`() = runBlocking {
        val details = SiteDetails.fetch("kurse.zhs-muenchen.de")
        assertEquals("ZHS München", details.name)
    }

    @Test
    fun `parse zhs`() {
        val html = runBlocking { Res.readBytes("files/http/zhs_front_page.html") }.decodeToString()

        val details = SiteDetails.parse(html)
        assertEquals("ZHS München", details.name)
        assertEquals("https://kurse.zhs-muenchen.de/services/image-proxy/rs:fit:192:192:1/plain/https://uninow-campus365-staging.s3.sbg.io.cloud.ovh.net/settings.management/kdamysccpykixszkuxtoorvcjgigcnba.png", details.icon)
    }

    @Test
    fun `parse aaa`() {
        val html = runBlocking { Res.readBytes("files/http/aaa_front_page.html") }.decodeToString()

        val details = SiteDetails.parse(html)
        assertEquals("Deutschkurse Buchungsplattform", details.name)
        assertEquals("https://aaa-giessen.uninow.com/services/image-proxy/rs:fit:192:192:1/plain/https://uninow-campus365-staging.s3.sbg.io.cloud.ovh.net/settings.management/ypsuldntspdqannpuneuiyvuyhbjumsv.png", details.icon)
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
