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
import de.bixilon.kutil.string.WhitespaceUtil.removeWhitespaces
import de.bixilon.unithen.api.HttpUtil
import de.bixilon.unithen.http.CLIENT
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

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

        private fun fetchIcon(url: String) = runBlocking { CLIENT.get(url).bodyAsBytes() }

        suspend fun fetch(host: String): SiteDetails {
            val request = HttpUtil.create(host, "/")

            val client = HttpClient(CIO) {
                install(HttpTimeout) { requestTimeoutMillis = 15.seconds.inWholeMilliseconds }
                followRedirects = true
            }
            try {

                val response = client.get(request)

                if (response.status != HttpStatusCode.OK) throw IllegalStateException("Request is not OK")

                return parse(response.bodyAsText(), this::fetchIcon)
            } finally {
                client.close()
            }
        }

        fun parse(html: String, fetcher: ((host: String) -> ByteArray)?): SiteDetails {
            val parsed = Ksoup.parse(html)

            val name = parsed.head()
                .getElementsByTag("title")
                .first()?.text() ?: throw IllegalStateException("Can not extract title!")



            parsed.body().getElementsMatchingText("in Magdeburg by").firstOrNull() ?: throw IllegalStateException("Not a uni now page (not in Magdeburg)!")


            val iconUrl = parsed.head()
                .getElementsByTag("link")
                .filter { it.attribute("rel")?.value == "icon" }
                .maxBy { it.attribute("sizes")?.value?.split("x")?.first()?.toInt() ?: 0 }
                .attribute("href")?.value
                ?.takeIf { it.endsWith(".png") }

            val icon = iconUrl?.let { fetcher?.invoke(it) }

            return SiteDetails(name, icon)
        }
    }
}
