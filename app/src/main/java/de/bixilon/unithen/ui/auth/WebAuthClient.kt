package de.bixilon.unithen.ui.auth

import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.util.CookieParser


open class WebAuthClient(
    val callback: (Authentication) -> Unit,
) : WebViewClient() {

    init {
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().removeAllCookie() // TODO: don't use deprecated cookie, store cookies only in memory
    }

    override fun onPageFinished(view: WebView, url: String) {
        val cookies = CookieManager.getInstance().getCookie(url)?.let(CookieParser::parse) ?: return

        val session = cookies["ory-session"] ?: return

        Log.v("WebView", "Found session cookie on $url")

        val authentication = CookieAuthentication(session)

        callback.invoke(authentication)
    }
}
