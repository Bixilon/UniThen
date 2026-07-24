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

package de.bixilon.unithen.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.UIKitView
import de.bixilon.unithen.api.HttpUtil
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.authentication.CookieAuthentication
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject


@Composable
actual fun WebAuthenticationView(host: String, callback: (Authentication) -> Unit) {
    var _host by remember { mutableStateOf("") }

    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            text = _host.takeIf { it.isNotBlank() } ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                val view = WKWebView()

                view.customUserAgent = HttpUtil.USER_AGENT
                view.navigationDelegate = WebViewUrlDelegate {
                    _host = it.host ?: ""

                    if (it.host != host) return@WebViewUrlDelegate

                    val cookies = view.configuration.websiteDataStore.httpCookieStore

                    cookies.getAllCookies {
                        val token = it?.filterIsInstance<NSHTTPCookie>()?.find { it.name == WEB_SESSION_COOKIE_NAME } ?: return@getAllCookies

                        view.loadHTMLString("<html>Logged in!</html>", null)
                        callback.invoke(CookieAuthentication(token.value()))
                    }
                }

                val url = NSURL.URLWithString("https://$host/auth/login")!!
                val request = NSURLRequest.requestWithURL(url)

                view.loadRequest(request)

                return@UIKitView view
            },
        )
    }
}

private class WebViewUrlDelegate(val onNavigate: (NSURL) -> Unit) : NSObject(), WKNavigationDelegateProtocol {

    override fun webView(webView: WKWebView, decidePolicyForNavigationAction: WKNavigationAction, decisionHandler: (WKNavigationActionPolicy) -> Unit) {
        webView.URL?.let(onNavigate)

        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
    }
}
