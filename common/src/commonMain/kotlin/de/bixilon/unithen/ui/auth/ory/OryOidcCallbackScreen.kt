package de.bixilon.unithen.ui.auth.ory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import de.bixilon.unithen.api.UniNowApi
import de.bixilon.unithen.api.authentication.OryTokenAuthentication
import de.bixilon.unithen.ui.containers.LoadingContainer
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.main.AuthenticationSyncRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.useAsyncNetwork


@Composable
fun OryOidcCallbackScreen(flowId: Int, code: String) {
    val navigator = LocalNavigation.current

    val flow = rememberStorage { flows[flowId] }
    if (flow == null) {
        SimpleErrorScreen("Invalid flow", "Is the return url correct or expired?")
        return
    }
    val site = rememberStorage { sites[flow.site]!! }

    val exchange = useAsyncNetwork {
        val api = UniNowApi(site.host)

        val token = api.exchangeToken(flow.exchangeToken!!, code)
        navigator.pop()
        navigator.navigate(AuthenticationSyncRoute(site, OryTokenAuthentication(token.sessionToken)))
    }

    LaunchedEffect(Unit) { exchange.invoke() }

    if (exchange.active) {
        LoadingContainer("Exchanging token...")
        return
    }

    ErrorBox("Something went wrong!")
}
