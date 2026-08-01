package de.bixilon.unithen.ui.auth.ory

import androidx.compose.runtime.Composable
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.api.UniNowApi
import de.bixilon.unithen.api.authentication.OryTokenAuthentication
import de.bixilon.unithen.ui.containers.LoadingContainer
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.main.AuthenticationCallbackRoute
import de.bixilon.unithen.ui.main.AuthenticationRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.useAsyncNetwork


@Composable
fun OryOidcCallbackScreen(flowId: Int, code: String) {
    val navigator = LocalNavigation.current
    val storage = LocalStorage.current

    val flow = rememberStorage { flows[flowId] }
    if (flow == null) {
        SimpleErrorScreen("Invalid authentication flow", "Maybe the url is not correct or it expired? Please try again!")
        return
    }
    val site = rememberStorage { sites[flow.site]!! }

    val exchange = useAsyncNetwork(true) {
        val api = UniNowApi(site.host)

        val token = api.exchangeToken(flow.exchangeToken!!, code)
        navigator.popIf { it is AuthenticationRoute }
        navigator.navigate(AuthenticationCallbackRoute(site, OryTokenAuthentication(token.sessionToken)))
        if (!RuntimeInfo.debug) {
            storage.flows.delete(flow.id)
        }
    }

    if (exchange.active) {
        LoadingContainer("Exchanging token...")
        return
    }

    ErrorBox("Something went wrong!")
}
