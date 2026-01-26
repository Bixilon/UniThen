package de.bixilon.unithen.api.user

import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.unithen.api.HttpUtil
import de.bixilon.unithen.api.HttpUtil.authenticate
import de.bixilon.unithen.api.authentication.Authentication
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.net.URI
import java.util.*

data class UserDetails(
    val uuid: UUID,
    val firstname: String,
    val lastname: String,
    val email: String,
) {


    companion object {
        private val USER_ID_REGEX = "id: \"([\\w-]{36})\"".toRegex()
        private val FIRSTNAME_REGEX = "first_name: \"(.+)\"".toRegex()
        private val LASTNAME_REGEX = "last_name: \"(.+)\"".toRegex()
        private val EMAIL_REGEX = "email: \"(.+@.+\\..+)\"".toRegex()

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
            val content = Jsoup.parse(html).head()
                .getElementsByTag("script")
                .find { it.data().contains("window.UniNow = ") }!!
                .data()

            val userId = USER_ID_REGEX.find(content)?.groupValues?.get(1)?.toUUID() ?: throw Error("Can not extract user id. Did UniNow change something in their response?")
            val firstname = FIRSTNAME_REGEX.find(content)!!.groupValues[1]
            val lastname = LASTNAME_REGEX.find(content)!!.groupValues[1]
            val email = EMAIL_REGEX.find(content)!!.groupValues[1]

            return UserDetails(userId, firstname, lastname, email)
        }
    }
}
