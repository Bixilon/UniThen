package de.bixilon.unithen.ui.main.setup

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.main.MainRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SetupScreenTest : AbstractComposeUiTest() {

    private fun ComposeUiTest.withSetupScreen() {
        setContent {
            CompositionLocalProvider(
                LocalNavigation provides remember { Navigator(MainRoute) },
            ) {
                SetupScreen()
            }
        }
    }

    @Test
    fun `display disclaimers properly`() = runComposeUiTest {
        withSetupScreen()

        waitUntilText("NOT affiliated").assertIsDisplayed()
        waitUntilText("privacy").assertIsDisplayed()
    }

    @Test
    fun `continue button only enabled if accepting terms`() = runComposeUiTest {
        withSetupScreen()

        waitUntilText("Continue to").assertIsNotEnabled()
        onNode(isToggleable() and hasClickAction() and hasAnySibling(hasText("I understand and", substring = true))).performClick()
        waitUntilText("Continue to").assertIsEnabled()
    }
}
