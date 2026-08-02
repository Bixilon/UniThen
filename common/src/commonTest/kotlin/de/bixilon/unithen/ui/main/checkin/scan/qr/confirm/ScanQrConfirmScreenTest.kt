package de.bixilon.unithen.ui.main.checkin.scan.qr.confirm

import androidx.compose.runtime.Composable
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
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.sync.LocalSyncEngine
import de.bixilon.unithen.ui.waitUntilText
import de.bixilon.unithen.util.Kutil.toUuid
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class ScanQrConfirmScreenTest : AbstractComposeUiTest() {

    @Composable
    private fun MockedScreen(storage: SqlStorage, appointment: Appointment = storage.appointments[901]!!, userId: String) {
        val sync = remember { SyncEngine(storage) {} }
        val navigator = remember { Navigator(MainRoute) }
        val store = remember { MemorySettingsStore() }

        CompositionLocalProvider(
            LocalNavigation provides navigator,
            LocalStorage provides storage,
            LocalSyncEngine provides sync,
            LocalSettingsStore provides store,
        ) {
            ScanQrConfirmScreen(appointment, remember { userId.toUuid() })
        }
    }

    @Test
    fun `not checked in known user`() = runComposeUiTest {
        val storage = dummy()

        setContent { MockedScreen(storage, userId = "00000000-0000-0000-0000-000000000002") }

        waitUntilText("Confirm").assertIsEnabled()
    }

    @Test
    fun `already checked in user`() = runComposeUiTest {
        val storage = dummy()

        setContent { MockedScreen(storage, userId = "00000000-0000-0000-0000-000000000006") }

        waitUntilText("Already").assertIsDisplayed()
        waitUntilText("Confirm").assertIsNotEnabled()
    }

    @Test
    fun `not enrolled user`() = runComposeUiTest {
        val storage = dummy()

        setContent { MockedScreen(storage, userId = "00000000-0000-0000-0000-000000000001") }

        waitUntilText("Not enrolled").assertIsDisplayed()
        waitUntilText("Confirm").assertIsNotEnabled()
    }

    @Test
    fun `unknown user`() = runComposeUiTest {
        val storage = dummy()

        setContent { MockedScreen(storage, userId = "10000000-0000-0000-0000-000000000001") }

        waitUntilText("Unknown user").assertIsDisplayed()
        waitUntilText("Confirm").assertIsNotEnabled()
    }

    @Test
    fun `updating enrolled list on unknown user`() = runComposeUiTest {
        val storage = dummy()

        setContent { MockedScreen(storage, userId = "10000000-0000-0000-0000-000000000001") }

        waitUntilText("Updating enrollment list").assertIsDisplayed()
    }

    @Test
    fun `already checked in and pending`() = runComposeUiTest {
        val storage = dummy()

        setContent { MockedScreen(storage, userId = "00000000-0000-0000-0000-000000000011") }

        waitUntilText("synchronization pending").assertIsDisplayed()
        waitUntilText("Confirm").assertIsNotEnabled()
    }

    @Test
    fun `already checked in and check out pending`() = runComposeUiTest {
        val storage = dummy()

        setContent { MockedScreen(storage, userId = "00000000-0000-0000-0000-000000000007") }

        waitUntilText("checkout pending").assertIsDisplayed()
        waitUntilText("Confirm").assertIsNotEnabled()
    }

    @Test
    fun `translated server error`() = runComposeUiTest {
        val storage = dummy()

        setContent { MockedScreen(storage, userId = "00000000-0000-0000-0000-000000000004") }

        waitUntilText("Booking not approved").assertIsDisplayed()
        waitUntilText("Confirm").assertIsNotEnabled()
    }

    @Test
    fun `check in known user`() = runComposeUiTest {
        val storage = dummy()

        val site = storage.sites[901]!!
        val user = storage.users[site, "00000000-0000-0000-0000-000000000002".toUuid()]!!
        val appointment = storage.appointments[901]!!

        setContent { MockedScreen(storage, userId = user.uuid.toString()) }

        waitUntilText("Confirm").performClick()
        waitUntilText("Confirm").assertIsNotEnabled()


        assertNotNull(storage.checkInQueue[appointment, user])
    }

    @Test
    fun `unknown user and known user but not enrolled after updating enrolled`() = runComposeUiTest {
        val storage = dummy()

        val uuid = "10000000-0000-0000-0000-000000000001".toUuid()
        val site = storage.sites[901]!!

        setContent { MockedScreen(storage, userId = uuid.toString()) }

        waitUntilText("Confirm")

        storage.users.add(site, uuid, "Hello", "world")

        waitUntilText("Not enrolled").isDisplayed()
        waitUntilText("Confirm").assertIsNotEnabled()
    }

    @Test
    fun `unknown user and known user enrolled after updating enrolled`() = runComposeUiTest {
        val storage = dummy()

        val uuid = "10000000-0000-0000-0000-000000000001".toUuid()
        val site = storage.sites[901]!!
        val appointment = storage.appointments[901]!!

        setContent { MockedScreen(storage, userId = uuid.toString()) }

        waitUntilText("Confirm")

        val user = storage.users.add(site, uuid, "Hello", "world")
        storage.courses.addEnrolled(user, storage.courses[appointment.course]!!)

        waitUntilText("Confirm").assertIsEnabled()
    }
}
