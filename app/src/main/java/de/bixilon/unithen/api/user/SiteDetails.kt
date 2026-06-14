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

import com.fleeksoft.ksoup.Ksoup
import de.bixilon.kutil.stream.InputStreamUtil.readAll
import de.bixilon.kutil.string.WhitespaceUtil.removeWhitespaces
import de.bixilon.kutil.uri.URIUtil.toURI
import de.bixilon.unithen.api.HttpUtil
import okhttp3.OkHttpClient
import java.net.URI

data class SiteDetails(
    val name: String,
    val icon: ByteArray?,
) {


    companion object {

        fun fix(url: String): String {
            val transformed = url
                .removeWhitespaces()
                .split("://", limit = 2).last()
                .split(":").first()
                .split("/").first()


            if (transformed.isBlank()) return ""

            if ("." !in transformed) throw IllegalArgumentException("Invalid host: $url")

            return transformed
        }

        private fun fetchIcon(url: URI) = url.toURL().openStream().readAll()

        suspend fun fetch(url: URI): SiteDetails {
            val request = HttpUtil.create(url, "/")
                .get()
                .build()

            val client = OkHttpClient().newBuilder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()

            val response = client.newCall(request).execute()

            if (response.code != 200) throw IllegalStateException("Request is not OK")

            return parse(response.body.string(), this::fetchIcon)
        }

        fun parse(html: String, fetcher: ((URI) -> ByteArray)?): SiteDetails {
            val parsed = Ksoup.parse(html)

            val name = parsed.head()
                .getElementsByTag("title")
                .first()?.text() ?: throw IllegalStateException("Can not extract title!")


            parsed.head()
                .getElementsByTag("script")
                .find { it.data().contains("window.UniNow = ") } ?: throw IllegalStateException("Not a uni now page!")


            parsed.body().getElementsMatchingText("in Magdeburg by").firstOrNull() ?: throw IllegalStateException("Not a uni now page!")


            val iconUrl = parsed.head()
                .getElementsByTag("link")
                .filter { it.attribute("rel")?.value == "icon" }
                .maxBy { it.attribute("sizes")?.value?.split("x")?.first()?.toInt() ?: 0 }
                .attribute("href")?.value
                ?.takeIf { it.endsWith(".png") }
                ?.toURI()

            val icon = iconUrl?.let { fetcher?.invoke(it) }

            return SiteDetails(name, icon)
        }
    }
}
