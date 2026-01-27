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

import android.graphics.Bitmap
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.util.KUtil.with
import java.net.URI


@Composable
fun WebAuthenticationView(modifier: Modifier = Modifier, base: URI, callback: (Authentication) -> Unit) {
    var view: WebView? by remember { mutableStateOf(null) }
    var canGoBack by remember { mutableStateOf(false) }

    BackHandler(enabled = canGoBack) { view?.goBack() }

    AndroidView(modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                view = this

                webViewClient = object : WebAuthClient({
                    settings.javaScriptEnabled = false
                    loadDataWithBaseURL("", "<html>Logged in!</html>", "text/html", "utf-8", "")
                    callback.invoke(it)
                }) {
                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        canGoBack = view.canGoBack()
                    }
                }

                settings.javaScriptEnabled = true
                loadUrl(base.with(path = "/auth/login").toString())
            }
        }
    )
}
