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

import de.bixilon.kutil.uri.URIUtil.with
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.api.authentication.Authentication
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import java.net.URI
import kotlin.time.Duration.Companion.seconds

object HttpUtil {
    val JSON = "application/json; charset=utf-8".toMediaType()

    suspend fun create(base: URI, endpoint: String): Request.Builder {
        assert(base.scheme == "https") { "Insecure requests are forbidden!" }
        if (BuildConfig.DEBUG) {
            delay(3.seconds)
        }
        val request = Request.Builder()
            .url(base.with(path = endpoint).toURL())
            .header("User-Agent", "UniThen (version=${BuildConfig.VERSION})")

        return request
    }

    fun Request.Builder.authenticate(authentication: Authentication) = apply { authentication.authenticate(this) }
}
