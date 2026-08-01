package de.bixilon.unithen.ui.main.checkin.present

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.settings.store.LocalSettingsStore
import de.bixilon.unithen.settings.store.MemorySettingsStore
import de.bixilon.unithen.storage.sql.dummy
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PresentQrScreenTest : AbstractComposeUiTest() {

    @Test
    fun `displayed correctly with valid appointment`() = runComposeUiTest {
        val storage = dummy()
        val account = storage.accounts[901]!!
        val course = storage.courses[901]!!
        val appointment = storage.appointments[901]!!

        setContent { CompositionLocalProvider(LocalSettingsStore provides remember { MemorySettingsStore() }) { PresentQrScreen(account, course, appointment) } }


        waitUntilText("First course", substring = false).assertIsDisplayed()
        waitUntilText("Hans Maulwurf", substring = false).assertIsDisplayed()
    }
}
