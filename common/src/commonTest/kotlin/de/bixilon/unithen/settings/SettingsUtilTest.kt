package de.bixilon.unithen.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.ui.AbstractComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

val BOOLEAN = Setting("boolean", true)
val INT = Setting("int", 0)

@OptIn(ExperimentalTestApi::class)
class SettingsUtilTest : AbstractComposeUiTest() {

    @Test
    fun `boolean settings are in sync`() = runComposeUiTest {

        setContent {
            var a by rememberSetting(BOOLEAN)
            var b by rememberSetting(BOOLEAN)

            assertEquals(a, b)
            a = !a
            assertEquals(a, b)
        }
    }

    @Test
    fun `int settings are in sync`() = runComposeUiTest {

        setContent {
            var a by rememberSetting(INT)
            var b by rememberSetting(INT)

            assertEquals(a, b)
            a++
            assertEquals(a, b)
        }
    }
}
