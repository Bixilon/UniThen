package de.bixilon.unithen.api

import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.unithen.api.HttpUtil.authenticate
import de.bixilon.unithen.api.authentication.Authentication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.jsoup.Jsoup
import java.net.URI
import java.util.*


object UniNowUtil {
    private val USER_ID_REGEX = "id: \"([\\w-]*)\",".toRegex()


    fun fetchUserId(url: URI, authentication: Authentication): UUID {
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

        return extractUserId(response.body.string())
    }

    fun extractUserId(html: String): UUID {
        val content = Jsoup.parse(html).head()
            .getElementsByTag("script")
            .find { it.data().contains("window.UniNow = ") }!!
            .data()

        return USER_ID_REGEX.find(content)!!.groupValues[1].toUUID()
    }
}
