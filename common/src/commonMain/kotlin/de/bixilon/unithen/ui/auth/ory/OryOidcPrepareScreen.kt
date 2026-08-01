package de.bixilon.unithen.ui.auth.ory

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import de.bixilon.unithen.ui.containers.LoadingContainer
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.useAsyncNetwork


@Composable
fun OryOidcPrepareScreen(ory: OryConfig, provider: OryConfig.OryOidc) {
    val navigator = LocalNavigation.current
    val handler = LocalUriHandler.current
    var url by remember { mutableStateOf<String?>(null) }

    val flowFetch = useAsyncNetwork {
        val response = ory.loginOidc(provider)

        handler.openUri(response.redirectBrowserTo)
        url = response.redirectBrowserTo
        navigator.pop()
    }

    LaunchedEffect(Unit) { flowFetch.invoke() }

    LoadingContainer("Getting oidc redirect url")
}
