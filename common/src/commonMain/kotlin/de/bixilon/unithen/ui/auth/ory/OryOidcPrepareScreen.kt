package de.bixilon.unithen.ui.auth.ory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import de.bixilon.kutil.string.WhitespaceUtil.removeWhitespaces
import de.bixilon.unithen.ui.containers.LoadingContainer
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.*
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.auth_oidc_complete
import unithen.common.generated.resources.auth_oidc_loading
import kotlin.time.Duration.Companion.seconds


@Composable
private fun Fallback() {
    val navigator = LocalNavigation.current
    Text("If you are on desktop or custom urls don't work on your device, please paste the url below (starting with uninow://)")

    val state = rememberTextFieldState()
    TextField(state, modifier = Modifier.fillMaxWidth(), lineLimits = TextFieldLineLimits.SingleLine)

    LaunchedEffect(state.text) {
        val raw = state.text.toString().removeWhitespaces()
        if (raw.startsWith("uninow://")) {
            navigator.handleUrl(raw)
        }
    }
}

@Composable
fun OryOidcPrepareScreen(ory: OryConfig, provider: OryConfig.OryOidc) {
    val handler = LocalUriHandler.current
    var url by remember { mutableStateOf<String?>(null) }

    val fetch = useAsyncNetwork(true) {
        val response = ory.loginOidc(provider)

        handler.openUri(response.redirectBrowserTo)
        url = response.redirectBrowserTo
    }

    if (fetch.active) {
        LoadingContainer(Res.string.auth_oidc_loading.i18n())
        return
    }

    Column {
        Text(Res.string.auth_oidc_complete.i18n())
        val foreground = rememberForeground()

        if (foreground) {
            DelayedContent(5.seconds) {
                Fallback()
            }
        }
    }
}
