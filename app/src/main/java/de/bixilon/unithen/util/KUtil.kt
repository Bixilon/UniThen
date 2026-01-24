package de.bixilon.unithen.util

import java.net.URI

object KUtil {

    @Deprecated("kutil 1.31")
    operator fun URI.div(path: String): URI = this.with(path = (this.path?.trimEnd('/') ?: "") + '/' + path.trimStart('/'))

    @Deprecated("kutil 1.31")
    fun URI.with(scheme: String? = this.scheme, userInfo: String? = this.userInfo, host: String? = this.host, port: Int = this.port, path: String = this.path, query: String? = this.query, fragment: String? = this.fragment): URI {
        return URI(scheme, userInfo, host, port, path, query, fragment)
    }
}
