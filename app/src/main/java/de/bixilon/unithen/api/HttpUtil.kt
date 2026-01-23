package de.bixilon.unithen.api

import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.api.authentication.Authentication
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import java.net.URI

object HttpUtil {
    val JSON = "application/json; charset=utf-8".toMediaType()

    fun create(base: URI, endpoint: String): Request.Builder {
        val request = Request.Builder()
            .url(base.resolve(endpoint).toURL()) // TODO: kutil /
            .header("User-Agent", "UniThen (version=${BuildConfig.VERSION})")

        return request
    }

    fun Request.Builder.authenticate(authentication: Authentication) = apply { authentication.authenticate(this) }
}
