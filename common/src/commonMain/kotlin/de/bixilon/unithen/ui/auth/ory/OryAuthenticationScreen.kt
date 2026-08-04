package de.bixilon.unithen.ui.auth.ory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
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
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.rememberAsync
import de.bixilon.unithen.ui.util.useAsyncNetwork
import de.bixilon.unithen.ui.util.verticalScrollbar
import unithen.common.generated.resources.*
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
        LoadingContainer(Res.string.auth_loading_methods.i18n())
        return
    }

    config?.let { WithFlow(host, it) } ?: FlowError(host)
}

@Composable
private fun FlowError(host: String) {
    val navigation = LocalNavigation.current

    Screen {
        ErrorBox(Res.string.auth_error_flow_title.i18n(), Res.string.auth_error_flow_description.i18n())
        Button({ navigation.navigate(LegacyAuthenticationRoute(host)) }, modifier = Modifier.fillMaxWidth()) { Text(Res.string.auth_try_legacy.i18n()) }
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
        val icon = rememberAsync { catchAll { Res.getUri("files/logo/${oidc.id.lowercase().replace("/", "")}.svg") } }
        val name = remember { Res.allStringResources["auth_oidc_provider_${oidc.id.lowercase().replace('-', '_')}"] }


        icon?.let { AsyncImage(it, "", modifier = Modifier.height(140.dp).padding(8.dp)) }

        Spacer(Modifier.height(4.dp))

        Text(name?.i18n() ?: oidc.id, modifier = Modifier.padding(5.dp), textAlign = TextAlign.Center)
    }
}


@Composable
private fun WithFlow(host: String, config: OryConfig) {
    val navigation = LocalNavigation.current
    val site = rememberStorage { sites[host]!! }

    if (config.oidc.isEmpty()) {
        return EmailAuthenticationScreen(site, config)
    }

    Screen {
        ScreenTitle(Res.string.auth_title.i18n())

        val scroll = rememberScrollState()
        Column(Modifier.verticalScroll(scroll).verticalScrollbar(scroll)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), maxItemsInEachRow = 2) {
                for (oidc in config.oidc) {
                    OidcCard(oidc) { navigation.navigate(OidcAuthenticationRoute(config, oidc)) }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button({ navigation.navigate(EmailAuthenticationRoute(site, config)) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Email, "")
                Spacer(Modifier.width(4.dp))
                Text(Res.string.auth_use_email.i18n())
            }
            Button({ navigation.navigate(LegacyAuthenticationRoute(host)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                Icon(Icons.Filled.Warning, "")
                Spacer(Modifier.width(4.dp))
                Text(Res.string.auth_try_legacy.i18n())
            }
        }
    }
}
