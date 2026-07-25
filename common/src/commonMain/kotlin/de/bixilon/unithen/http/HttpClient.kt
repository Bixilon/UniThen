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

package de.bixilon.unithen.http

import de.bixilon.unithen.api.errors.NetworkException
import io.ktor.client.*
import io.ktor.client.plugins.*
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import kotlin.time.Duration.Companion.seconds

val CLIENT by lazy {
    HttpClient {
        install(HttpTimeout) { connectTimeoutMillis = 10.seconds.inWholeMilliseconds; requestTimeoutMillis = 60.seconds.inWholeMilliseconds }
        HttpResponseValidator {
            handleResponseException {
                when (it) {
                    is UnresolvedAddressException -> throw NetworkException(it)
                    is IOException -> throw NetworkException(it)
                }
                throw it
            }
        }
        followRedirects = false
    }
}
