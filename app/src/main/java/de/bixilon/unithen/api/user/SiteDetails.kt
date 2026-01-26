package de.bixilon.unithen.api.user

import de.bixilon.kutil.uri.URIUtil.toURI
import de.bixilon.unithen.api.HttpUtil
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.net.URI

typealias SHA1 = String

data class SiteDetails(
    val name: String,
    val icon: ByteArray?,
) {


    companion object {

        fun fix(url: String) = "https://${url.toURI().host}".toURI()

        fun fetch(url: URI): SiteDetails {
            val request = HttpUtil.create(url, "/")
                .get()
                .build()

            val client = OkHttpClient().newBuilder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()

            val response = client.newCall(request).execute()

            if (response.code != 200) throw IllegalStateException("Request is not OK")

            return parse(response.body.string(), null) // TODO: Icon
        }

        fun parse(html: String, fetcher: ((URI) -> ByteArray)?): SiteDetails {
            val parsed = Jsoup.parse(html)

            val name = parsed.head()
                .getElementsByTag("title")
                .first()?.text() ?: throw IllegalStateException("Can not extract title!")

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
