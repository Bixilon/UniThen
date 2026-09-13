package de.bixilon.unithen.ui.main.site

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.storage.sql.dummy
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.main.MainRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SelectSiteScreenTest : AbstractComposeUiTest() {

    private fun ComposeUiTest.withSelectSiteScreen(callback: (Site) -> Unit = {}) {
        setContent {
            CompositionLocalProvider(
                LocalNavigation provides remember { Navigator(MainRoute) },
                LocalStorage provides remember { dummy() }
            ) {
                SelectSiteScreen(callback)
            }
        }
    }

    @Test
    fun `sites get synced with internal storage`() = runComposeUiTest {
        withSelectSiteScreen()

        waitUntilText("ZHS").assertIsDisplayed()
    }

    @Test
    fun `test local present`() = runComposeUiTest {
        withSelectSiteScreen()

        waitUntilText("test.local").assertIsDisplayed()
    }
}
