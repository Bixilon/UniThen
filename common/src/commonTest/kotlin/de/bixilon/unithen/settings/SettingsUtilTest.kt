package de.bixilon.unithen.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.unithen.settings.store.LocalSettingsStore
import de.bixilon.unithen.settings.store.SettingsStore
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.waitUntilText
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

            Text(a.toString())
            LaunchedEffect(Unit) { a = false }
        }
        waitUntilText("false").assertIsDisplayed()
    }

    @Test
    fun `int settings are in sync`() = runComposeUiTest {
        withStore {
            var a by rememberSetting(INT)
            var b by rememberSetting(INT)

            assertEquals(a, b)
            Text(a.toString())

            LaunchedEffect(Unit) { a = 1 }
        }
        waitUntilText("1").assertIsDisplayed()
    }

    @Test
    fun `string settings are in sync`() = runComposeUiTest {
        withStore {
            var a by rememberSetting(STRING)
            var b by rememberSetting(STRING)

            assertEquals(a, b)
            Text(a)
            LaunchedEffect(Unit) { a = "something" }
        }
        waitUntilText("something").assertIsDisplayed()
    }

    @Test
    fun `enum settings are in sync`() = runComposeUiTest {
        withStore {
            var a by rememberSetting(ENUM)
            var b by rememberSetting(ENUM)

            assertEquals(a, b)
            Text(a.toString())
            LaunchedEffect(Unit) { a = TestEnum.C }
        }
        waitUntilText("C").assertIsDisplayed()
    }
}
