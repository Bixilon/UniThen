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

package de.bixilon.unithen.api

import de.bixilon.unithen.BuildInfo
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.api.authentication.Authentication
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

object HttpUtil {
    val USER_AGENT = "UniThen (version=${BuildInfo.VERSION})"

    suspend fun create(host: String, endpoint: String): HttpRequestBuilder {
        if (RuntimeInfo.debug) {
            delay(3.seconds)
        }
        val request = HttpRequestBuilder()
        request.apply {
            url {
                this.host = host
                pathSegments = endpoint.split("/")
            }
            headers.apply { set("User-Agent", USER_AGENT) }
        }

        return request
    }

    fun HttpRequestBuilder.authenticate(authentication: Authentication) = apply { authentication.authenticate(this) }
}
