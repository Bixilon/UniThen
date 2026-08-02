package de.bixilon.unithen.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.unithen.ui.main.AuthenticationRoute
import de.bixilon.unithen.ui.main.OidcAuthenticationCallbackRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import io.ktor.http.*

val LocalUrlIntent = staticCompositionLocalOf<String?> { null }

fun Navigator.handleUrl(url: String?) {
    val url = url?.let { catchAll { Url(it) } } ?: return
    if (url.protocol.name.lowercase() == "uninow" && url.host.lowercase() == "course" && url.rawSegments.getOrNull(1)?.lowercase() == "login") {
        val code = url.parameters["code"]
        val flowId = url.parameters["unithen"]?.toIntOrNull()

        if (code == null || flowId == null) return

        popIf { it is AuthenticationRoute }
        navigate(OidcAuthenticationCallbackRoute(flowId, code))
    }
}

@Composable
fun LocalUrlHandler() {
    val navigator = LocalNavigation.current
    val url = LocalUrlIntent.current

    LaunchedEffect(url) { navigator.handleUrl(url) }
}
