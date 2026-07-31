package de.bixilon.unithen.settings

import androidx.compose.runtime.*
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.settings.store.LocalSettingsStore
import de.bixilon.unithen.settings.store.SettingsStore
import de.bixilon.unithen.ui.AbstractComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

val BOOLEAN = Setting("boolean", true)
val INT = Setting("int", 0)

expect fun createSettingsStore(): SettingsStore

@OptIn(ExperimentalTestApi::class)
class SettingsUtilTest : AbstractComposeUiTest() {


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
    fun `boolean settings are in sync`() = runComposeUiTest {
        withStore {
            var a by rememberSetting(BOOLEAN)
            var b by rememberSetting(BOOLEAN)

            assertEquals(a, b)
            a = !a
            assertEquals(a, b)
        }
    }

    @Test
    fun `int settings are in sync`() = runComposeUiTest {
        withStore {
            var a by rememberSetting(INT)
            var b by rememberSetting(INT)

            assertEquals(a, b)
            a++
            assertEquals(a, b)
        }
    }
}
