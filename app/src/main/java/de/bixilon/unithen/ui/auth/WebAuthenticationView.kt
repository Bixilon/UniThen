package de.bixilon.unithen.ui.auth

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.util.KUtil.with
import java.net.URI


@Composable
fun WebAuthenticationView(modifier: Modifier = Modifier, base: URI, callback: (Authentication) -> Unit) = AndroidView(modifier = modifier,
    factory = { context ->
        WebView(context).apply {
            webViewClient = WebAuthClient {
                settings.javaScriptEnabled = false
                loadDataWithBaseURL("", "<html>Logged in!</html>", "text/html", "utf-8", "");
                callback.invoke(it)
            }

            settings.javaScriptEnabled = true
            loadUrl(base.with(path = "/auth/login").toString())
        }
    }
)
