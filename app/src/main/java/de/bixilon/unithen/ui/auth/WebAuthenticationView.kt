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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.kutil.uri.URIUtil.toURI
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.util.KUtil.with
import java.net.URI


@Composable
fun WebAuthenticationView(url: URI, callback: (Authentication) -> Unit) {
    var view: WebView? by remember { mutableStateOf(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var host by remember { mutableStateOf("") }

    BackHandler(enabled = canGoBack) { view?.goBack() }

    Column {
        if (host.isNotBlank()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                text = host,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }

        AndroidView(modifier = Modifier.fillMaxHeight(), factory = { context ->
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
                        host = catchAll { url.toURI().host } ?: ""
                    }
                }

                settings.javaScriptEnabled = true
                loadUrl(url.with(path = "/auth/login").toString())
            }
        }
        )
    }
}
