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

package de.bixilon.unithen.api.authentication

import de.bixilon.unithen.ui.auth.WEB_SESSION_COOKIE_NAME
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val COOKIE_TYPE = "cookie"

@Serializable
@SerialName(COOKIE_TYPE)
data class CookieAuthentication(
    val token: String,
) : Authentication {
    override val type get() = COOKIE_TYPE

    init {
        if (token.isBlank()) throw IllegalArgumentException("Cookie is blank!")
    }

    override fun authenticate(request: HttpRequestBuilder) {
        request.headers["Cookie"] = "${WEB_SESSION_COOKIE_NAME}=$token"
    }
}
