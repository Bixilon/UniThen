package de.bixilon.unithen.ui.util

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.ui.main.MainRoute
import de.bixilon.unithen.ui.main.OidcAuthenticationCallbackRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LocalUrlHandlerTest {


    private fun ComposeUiTest.withHandler(url: String?) {
        setContent {
            val navigator = Navigator(MainRoute)

            navigator.Routes {
                composable<MainRoute> { Text("Main") }
                composable<OidcAuthenticationCallbackRoute> { Text("OIDC"); Text("flow: ${it.flow}"); Text("code: ${it.code}") }
            }

            CompositionLocalProvider(
                LocalUrlIntent provides url,
                LocalNavigation provides navigator,
            ) {
                navigator.Host()
                LocalUrlHandler()
            }
        }
    }

    @Test
    fun `malformed url`() = runComposeUiTest {
        withHandler("something is not right")

        waitUntilText("Main").assertIsDisplayed()
    }

    @Test
    fun `course login return to url`() = runComposeUiTest {
        withHandler("uninow://COURSE/login?code=abc&unithen=5")

        waitUntilText("OIDC").assertIsDisplayed()
        waitUntilText("flow: 5").assertIsDisplayed()
        waitUntilText("code: abc").assertIsDisplayed()
    }
}
