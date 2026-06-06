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

package de.bixilon.unithen.storage.types

import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.storage.DbKeyed
import de.bixilon.unithen.storage.Key
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

data class Account(
    override val id: Key,
    val site: Key,
    val uuid: UUID,

    val firstname: String,
    val lastname: String,

    @Deprecated("multiple authentication methods")
    val sessionKey: String?,

    val fetched: Instant,
) : DbKeyed {

    fun api(site: Site): AuthenticatedUniNowApi {
        if (sessionKey.isNullOrBlank()) throw AuthenticationException("Authentication cookie is blank!")

        return AuthenticatedUniNowApi(site.url, CookieAuthentication(sessionKey))
    }


    fun isStale(now: Instant = Clock.System.now()) = now - fetched > ACCOUNT_CACHE_TTL

    companion object {
        val ACCOUNT_CACHE_TTL = 5.minutes
    }
}
