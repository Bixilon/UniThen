package de.bixilon.unithen.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.unithen.ui.main.AuthenticationRoute
import de.bixilon.unithen.ui.main.OidcAuthenticationCallbackRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import io.ktor.http.*

val LocalUrlIntent = staticCompositionLocalOf<String?> { null }


@Composable
fun LocalUrlHandler() {
    val navigator = LocalNavigation.current
    val uri = LocalUrlIntent.current

    LaunchedEffect(uri) {
        val url = uri?.let { catchAll { Url(it) } } ?: return@LaunchedEffect
        if (url.protocol.name == "uninow" && url.host == "COURSE" && url.rawSegments.getOrNull(1) == "login") {
            val code = url.parameters["code"]
            val flowId = url.parameters["unithen"]?.toIntOrNull()

            if (code == null || flowId == null) return@LaunchedEffect

            navigator.popIf { it is AuthenticationRoute }
            navigator.navigate(OidcAuthenticationCallbackRoute(flowId, code))
        }
    }
}
