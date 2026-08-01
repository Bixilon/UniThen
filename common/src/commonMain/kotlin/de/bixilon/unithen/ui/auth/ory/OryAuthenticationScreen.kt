package de.bixilon.unithen.ui.auth.ory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import de.bixilon.unithen.api.UniNowApi
import de.bixilon.unithen.ui.containers.LoadingContainer
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.main.EmailAuthenticationRoute
import de.bixilon.unithen.ui.main.LegacyAuthenticationRoute
import de.bixilon.unithen.ui.main.OidcAuthenticationRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.useAsyncNetwork
import unithen.common.generated.resources.Res
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@Composable
fun OryAuthenticationScreen(host: String) {
    val storage = LocalStorage.current
    var config by remember { mutableStateOf<OryConfig?>(null) }

    val stored = remember { storage.flows.create(storage.sites[host]!!, Clock.System.now() + 1.hours) }

    val flowFetch = useAsyncNetwork(true) {
        val fetched = UniNowApi(host).getLoginFlow(stored.id)
        storage.flows.update(stored.id, fetched.sessionTokenExchangeToken)
        config = fetched.toConfig()
    }

    if (flowFetch.active) {
        LoadingContainer("Fetching authentication methods...")
        return
    }

    config?.let { WithFlow(host, it) } ?: FlowError(host)
}

@Composable
private fun FlowError(host: String) {
    val navigation = LocalNavigation.current

    Screen {
        ScreenTitle("Error")
        ErrorBox("Error fetching login flow!")
        Button({ navigation.navigate(LegacyAuthenticationRoute(host)) }, modifier = Modifier.fillMaxWidth()) { Text("Try legacy login") }
    }
}

@Composable
fun FlowRowScope.OidcCard(oidc: OryConfig.OryOidc, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .padding(4.dp)
        .clip(RoundedCornerShape(8.dp))
        .height(200.dp)
        .weight(1f)
        .background(MaterialTheme.colorScheme.primaryContainer)
        .clickable { onClick.invoke() }
    ) {
        val icon = remember { OidcProviders.LOGOS[oidc.id] }
        val name = remember { OidcProviders.NAMES[oidc.id] ?: oidc.id }


        icon?.let { AsyncImage(Res.getUri("files/logo/${it}"), "", modifier = Modifier.height(150.dp).padding(8.dp)) }

        Spacer(Modifier.height(4.dp))

        Text(name, textAlign = TextAlign.Center)
    }
}


@Composable
private fun WithFlow(host: String, config: OryConfig) {
    val navigation = LocalNavigation.current
    val site = rememberStorage { sites[host]!! }

    if (config.oidc.isEmpty()) {
        return EmailAuthenticationScreen(site, config)
    }

    Screen(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ScreenTitle("Authentication")

        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), maxItemsInEachRow = 2) {
            for (oidc in config.oidc) {
                OidcCard(oidc) { navigation.navigate(OidcAuthenticationRoute(config, oidc)) }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button({ navigation.navigate(EmailAuthenticationRoute(site, config)) }, modifier = Modifier.fillMaxWidth()) { Text("Login with email") }
        Button({ navigation.navigate(LegacyAuthenticationRoute(host)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) { Text("Legacy login") }
    }
}
