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
