package de.bixilon.unithen.ui.main.checkin.scan

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.settings.store.LocalSettingsStore
import de.bixilon.unithen.settings.store.MemorySettingsStore
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.dummy
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.sync.SyncEngine
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.main.MainRoute
import de.bixilon.unithen.ui.main.checkin.scan.attendees.ScanAttendeeList
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.sync.LocalSyncEngine
import de.bixilon.unithen.ui.waitUntilText
import de.bixilon.unithen.ui.waitUntilTextGone
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScanAttendeeListTest : AbstractComposeUiTest() {

    private fun ComposeUiTest.withAttendeeList(storage: SqlStorage, appointment: Appointment) {
        setContent {
            CompositionLocalProvider(
                LocalStorage provides storage,
                LocalSyncEngine provides remember { SyncEngine(storage) {} },
                LocalNavigation provides remember { Navigator(MainRoute) },
                LocalSettingsStore provides remember { MemorySettingsStore() },
            ) {
                ScanAttendeeList(appointment)
            }
        }
    }

    @Test
    fun `display cards correctly`() = runComposeUiTest {
        val storage = dummy()
        val appointment = storage.appointments[901]!!
        withAttendeeList(storage, appointment)

        waitUntilText("Leonie Kurz").assertIsDisplayed()
        waitUntilText("Gustaf Maier").assertIsDisplayed()
        waitUntilText("Peter Wurst").assertIsDisplayed()
    }

    @Test
    fun `filter by name`() = runComposeUiTest {
        val storage = dummy()
        val appointment = storage.appointments[901]!!
        withAttendeeList(storage, appointment)


        onNode(hasText("Search", substring = true)).performTextInput("e")

        waitUntilText("Eva Klug").assertIsDisplayed()
        waitUntilText("Emilia Gans").assertIsDisplayed()

        waitUntilTextGone("Leonie Kurz")
        waitUntilTextGone("Gustaf Maier")
        waitUntilTextGone("Peter Wurst")
    }
}
