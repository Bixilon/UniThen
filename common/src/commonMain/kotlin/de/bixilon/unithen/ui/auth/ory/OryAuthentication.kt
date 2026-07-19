package de.bixilon.unithen.ui.auth.ory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.UniNowApi
import de.bixilon.unithen.api.ory.LoginFlow
import de.bixilon.unithen.ui.containers.LoadingContainer
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.main.EmailAuthenticationRoute
import de.bixilon.unithen.ui.main.LegacyAuthenticationRoute
import de.bixilon.unithen.ui.main.OidcAuthenticationRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.useAsyncNetwork


@Composable
fun OryAuthentication(host: String) {
    var flow by remember { mutableStateOf<LoginFlow?>(null) }

    val flowFetch = useAsyncNetwork<Unit>(null) { flow = UniNowApi(host).login() }

    LaunchedEffect(Unit) { flowFetch.invoke(Unit) }

    if (flowFetch.active) {
        LoadingContainer("Fetching authentication methods...")
        return
    }

    flow?.let { WithFlow(host, it) } ?: FlowError(host)
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
private fun WithFlow(host: String, flow: LoginFlow) {
    val navigation = LocalNavigation.current
    val config = remember { flow.toConfig() }

    if (config.oidc.isEmpty()) {
        return EmailAuthenticationScreen(config)
    }

    Screen(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ScreenTitle("Authentication")

        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), maxItemsInEachRow = 2) {
            for (oidc in config.oidc) {
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .height(250.dp)
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { navigation.navigate(OidcAuthenticationRoute(config, oidc)) }
                ) {
                    val icon = OidcProviders.LOGOS[oidc.id] ?: Icons.Default.QuestionMark
                    Image(icon, "", modifier = Modifier.height(200.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(OidcProviders.NAMES[oidc.id] ?: oidc.id)
                }
            }
        }

        Button({ navigation.navigate(EmailAuthenticationRoute(config)) }, modifier = Modifier.fillMaxWidth()) { Text("Login with email") }
        Button({ navigation.navigate(LegacyAuthenticationRoute(host)) }, modifier = Modifier.fillMaxWidth()) { Text("Try legacy login") }
    }
}
