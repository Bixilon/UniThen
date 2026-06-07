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
import de.bixilon.unithen.api.HttpUtil
import de.bixilon.unithen.api.HttpUtil.authenticate
import de.bixilon.unithen.api.authentication.Authentication
import okhttp3.OkHttpClient
import java.net.URI
import kotlin.uuid.Uuid

data class UserDetails(
    val uuid: Uuid,
    val firstname: String,
    val lastname: String,
    @Deprecated("unused") val email: String,
) {


    companion object {
        private val USER_ID_REGEX = "id: \"([\\w-]{36})\"".toRegex() // TODO: extractable with get_payer, but that sucks.
        private val FIRSTNAME_REGEX = "first_name: \"(.+)\"".toRegex() // TODO: extract via user_pk->first_name
        private val LASTNAME_REGEX = "last_name: \"(.+)\"".toRegex() // TODO: extract via user_pk->last_name
        private val EMAIL_REGEX = "email: \"(.+@.+\\..+)\"".toRegex() // TODO: extract via user_pk->email

        fun fetch(url: URI, authentication: Authentication): UserDetails {
            val request = HttpUtil.create(url, "/")
                .authenticate(authentication)
                .get()
                .build()

            val client = OkHttpClient().newBuilder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()

            val response = client.newCall(request).execute()

            if (response.code != 200) throw IllegalStateException("Request is not OK")

            return parse(response.body.string())
        }

        fun parse(html: String): UserDetails {
            // TODO: parse json, failover: user-nav
            val content = Ksoup.parse(html).head()
                .getElementsByTag("script")
                .find { it.data().contains("window.UniNow = ") }!!
                .data()

            val userId = USER_ID_REGEX.find(content)?.groupValues?.get(1)?.let { Uuid.parse(it) } ?: throw Error("Can not extract user id. Did UniNow change something in their response?")
            val firstname = FIRSTNAME_REGEX.find(content)!!.groupValues[1]
            val lastname = LASTNAME_REGEX.find(content)!!.groupValues[1]
            val email = EMAIL_REGEX.find(content)!!.groupValues[1]

            return UserDetails(userId, firstname, lastname, email)
        }
    }
}
