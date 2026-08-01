package de.bixilon.unithen.ui.auth.ory

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.main.MainRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test
import kotlin.time.Instant
import kotlin.uuid.Uuid

@OptIn(ExperimentalTestApi::class)
class EmailAuthenticationScreenTest : AbstractComposeUiTest() {

    private fun ComposeUiTest.withMockScreen() {
        val site = Site(1, "test.de", "na", null, Instant.DISTANT_PAST)
        val config = OryConfig(Uuid.random(), "test", listOf())
        setContent {
            CompositionLocalProvider(
                LocalNavigation provides Navigator(MainRoute)
            ) {
                EmailAuthenticationScreen(site, config)
            }
        }
    }

    @Test
    fun `login button is enabled when entering correct details`() = runComposeUiTest {
        withMockScreen()

        waitUntilText("Email", matcher = isEditable(), substring = false).performTextInput("test@test.de")
        waitUntilText("Password", matcher = isEditable(), substring = false).performTextInput("secret")

        waitUntilText("Login", matcher = hasClickAction()).assertIsEnabled()
    }
}
