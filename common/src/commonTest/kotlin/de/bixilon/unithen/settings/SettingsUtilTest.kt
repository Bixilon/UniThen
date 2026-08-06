package de.bixilon.unithen.settings

import androidx.compose.runtime.*
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.unithen.settings.store.LocalSettingsStore
import de.bixilon.unithen.settings.store.SettingsStore
import de.bixilon.unithen.ui.AbstractComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

val BOOLEAN = Setting("boolean", true)
val INT = Setting("int", 0)
val STRING = Setting("string", "abc")
val ENUM = EnumSetting("enum", TestEnum.A, TestEnum)

enum class TestEnum {
    A,
    B,
    C,
    ;

    companion object : ValuesEnum<TestEnum> {
        override val VALUES = values()
        override val NAME_MAP = names()
    }
}

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
            LaunchedEffect(Unit) {
                a = !a
                assertEquals(false, a)
                assertEquals(a, b)
            }
        }
    }

    @Test
    fun `int settings are in sync`() = runComposeUiTest {
        withStore {
            var a by rememberSetting(INT)
            var b by rememberSetting(INT)

            assertEquals(a, b)
            LaunchedEffect(Unit) {
                a++
                assertEquals(1, a)
                assertEquals(a, b)
            }
        }
    }

    @Test
    fun `string settings are in sync`() = runComposeUiTest {
        withStore {
            var a by rememberSetting(STRING)
            var b by rememberSetting(STRING)

            assertEquals(a, b)
            LaunchedEffect(Unit) {
                a = "something"
                assertEquals("something", a)
                assertEquals(a, b)
            }
        }
    }

    @Test
    fun `enum settings are in sync`() = runComposeUiTest {
        withStore {
            var a by rememberSetting(ENUM)
            var b by rememberSetting(ENUM)

            assertEquals(a, b)
            LaunchedEffect(Unit) {
                a = TestEnum.C
                assertEquals(TestEnum.C, a)
                assertEquals(a, b)
            }
        }
    }
}
