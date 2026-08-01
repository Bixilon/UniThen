package de.bixilon.unithen.ui.main.courses

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.storage.sql.dummy
import de.bixilon.unithen.sync.SyncEngine
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.main.MainRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.sync.LocalSyncEngine
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CoursesScreenTest : AbstractComposeUiTest() {

    @Test
    fun `all courses with events`() = runComposeUiTest {
        val storage = dummy()

        setContent {
            CompositionLocalProvider(
                LocalNavigation provides remember { Navigator(MainRoute) },
                LocalSyncEngine provides remember { SyncEngine(storage) {} },
                LocalStorage provides storage,
            ) {
                CoursesScreen()
            }
        }


        waitUntilText("Test Event (a) (2)").assertIsDisplayed()
        waitUntilText("First course", substring = false).assertIsDisplayed()
        waitUntilText("Unreferenced course", substring = false).assertIsDisplayed()
    }
}
