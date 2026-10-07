package de.bixilon.unithen.ui.main.checkin.scan.search

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.settings.store.LocalSettingsStore
import de.bixilon.unithen.settings.store.MemorySettingsStore
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.dummy
import de.bixilon.unithen.sync.SyncEngine
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.main.MainRoute
import de.bixilon.unithen.ui.main.checkin.scan.attendees.UserSearchScreen
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.sync.LocalSyncEngine
import de.bixilon.unithen.ui.waitUntilText
import de.bixilon.unithen.ui.waitUntilTextGone
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class SearchUserScreenTest : AbstractComposeUiTest() {

    private fun ComposeUiTest.withUserSearch(storage: SqlStorage) {
        setContent {
            CompositionLocalProvider(
                LocalStorage provides storage,
                LocalSyncEngine provides remember { SyncEngine(storage) },
                LocalNavigation provides remember { Navigator(MainRoute) },
                LocalSettingsStore provides remember { MemorySettingsStore() },
            ) {
                UserSearchScreen()
            }
        }
    }

    @Test
    fun `display names correctly`() = runComposeUiTest {
        val storage = dummy()
        withUserSearch(storage)

        waitUntilText("Leonie Kurz").assertIsDisplayed()
        waitUntilText("Gustaf Maier").assertIsDisplayed()
        waitUntilText("Peter Wurst").assertIsDisplayed()
        waitUntilTextGone("Hans Maulwurf")
    }

    @Test
    fun `single course only single course`() = runComposeUiTest {
        val storage = dummy()
        withUserSearch(storage)


        onNode(hasText("Search")).performTextInput("Leonie")

        waitUntilText("First course").assertIsDisplayed()

        waitUntilTextGone("Third course")
    }

    @Test
    fun `multiple courses all displayed`() = runComposeUiTest {
        val storage = dummy()
        withUserSearch(storage)


        onNode(hasText("Search")).performTextInput("Emilia")

        waitUntilText("First course").assertIsDisplayed()
        waitUntilText("Third course").assertIsDisplayed()
    }

    @Test
    fun `filter by name`() = runComposeUiTest {
        val storage = dummy()
        withUserSearch(storage)


        onNode(hasText("Search")).performTextInput("e")

        waitUntilText("Eva Klug", substring = false).assertIsDisplayed()
        waitUntilText("Emilia Gans", substring = false).assertIsDisplayed()

        waitUntilTextGone("Leonie Kurz")
        waitUntilTextGone("Gustaf Maier")
        waitUntilTextGone("Peter Wurst")
    }

    @Test
    fun `tick off enrolled user and add to checkin queue`() = runComposeUiTest {
        val storage = dummy()
        val appointment = storage.appointments[901]
        val user = storage.users[903]
        withUserSearch(storage)


        waitUntilText("Emilia Gans", substring = false).onParent().onChildAt(1).onChildAt(0).onChildAt(0).performClick()
        waitForIdle()

        assertNotNull(storage.checkInQueue[appointment, user])
    }

    @Test
    fun `tick off enrolled user second course and add to checkin queue`() = runComposeUiTest {
        val storage = dummy()
        val appointment = storage.appointments[905]
        val user = storage.users[905]
        withUserSearch(storage)


        waitUntilText("Emilia Gans", substring = false).onParent().onChildAt(1).onChildAt(1).onChildAt(1).onChildAt(0).performClick()
        waitForIdle()

        assertNotNull(storage.checkInQueue[appointment, user])
    }

    @Test
    fun `tick off attendee and add to checkout queue`() = runComposeUiTest {
        val storage = dummy()
        val appointment = storage.appointments[901]
        val user = storage.users[906]
        withUserSearch(storage)


        waitUntilText("Emilia Gans", substring = false).onParent().onChildAt(1).onChildAt(0).onChildAt(1).onChildAt(0).performClick()
        waitForIdle()

        assertNotNull(storage.checkInQueue[appointment, user]?.attempt)
    }
}
