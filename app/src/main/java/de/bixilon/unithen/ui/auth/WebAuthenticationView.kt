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
