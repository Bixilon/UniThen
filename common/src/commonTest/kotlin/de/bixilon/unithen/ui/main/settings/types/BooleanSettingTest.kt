package de.bixilon.unithen.ui.main.settings.types

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.settings.Setting
import de.bixilon.unithen.settings.createSettingsStore
import de.bixilon.unithen.settings.key
import de.bixilon.unithen.settings.store.LocalSettingsStore
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.waitUntil
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class BooleanSettingTest : AbstractComposeUiTest() {

    private fun ComposeUiTest.withStore(block: @Composable () -> Unit) {
        setContent {
            val store = remember { createSettingsStore() }
            CompositionLocalProvider(
                LocalSettingsStore provides store,
            ) {
                block.invoke()
            }
        }
    }

    @Test
    fun `boolean setting true`() = runComposeUiTest {
        val setting = Setting(key(), true)
        withStore {
            BooleanSetting(setting, "Title", "Description")
        }

        waitUntilText("Title").assertIsDisplayed()
        waitUntil(isToggleable()).assertIsEnabled()
        waitUntil(isToggleable()).assertIsOn()
    }

    @Test
    fun `boolean setting toggle on click`() = runComposeUiTest {
        val setting = Setting(key(), true)

        withStore {
            BooleanSetting(setting, "Title", "Description")
        }

        waitUntilText("Title").assertIsDisplayed()
        waitUntil(isToggleable()).performClick()
        waitUntil(isToggleable()).assertIsOff()
    }
}
