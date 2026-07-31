package de.bixilon.unithen.ui.auth.ory

import androidx.compose.runtime.*
import de.bixilon.unithen.ui.containers.LoadingContainer
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.useAsyncNetwork


@Composable
fun OryOidcPrepareScreen(ory: OryConfig, provider: OryConfig.OryOidc) {
    val navigator = LocalNavigation.current
    var url by remember { mutableStateOf<String?>(null) }

    val flowFetch = useAsyncNetwork { url = "" } // TODO


    if (flowFetch.active) {
        LoadingContainer("Getting oidc redirect url")
        return
    }

    LaunchedEffect(Unit) { navigator.pop() } // TODO: Show loading screen until callback is called
}
