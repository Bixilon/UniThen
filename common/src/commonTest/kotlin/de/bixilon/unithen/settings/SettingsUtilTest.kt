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
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

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

fun key() = "test${Random.nextLong()}"

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
        val setting = Setting(key(), true)
        withStore {
            var a by rememberSetting(setting)
            var b by rememberSetting(setting)

            assertEquals(a, b)

            Text(a.toString())
            LaunchedEffect(Unit) { a = false }
        }
        waitUntilText("false").assertIsDisplayed()
    }

    @Test
    fun `int settings are in sync`() = runComposeUiTest {
        val setting = Setting(key(), 0)
        withStore {
            var a by rememberSetting(setting)
            var b by rememberSetting(setting)

            assertEquals(a, b)
            Text(a.toString())

            LaunchedEffect(Unit) { a = 1 }
        }
        waitUntilText("1").assertIsDisplayed()
    }

    @Test
    fun `string settings are in sync`() = runComposeUiTest {
        val setting = Setting(key(), "abc")
        withStore {
            var a by rememberSetting(setting)
            var b by rememberSetting(setting)

            assertEquals(a, b)
            Text(a)
            LaunchedEffect(Unit) { a = "something" }
        }
        waitUntilText("something").assertIsDisplayed()
    }

    @Test
    fun `enum settings are in sync`() = runComposeUiTest {
        val setting = EnumSetting(key(), TestEnum.A, TestEnum)
        withStore {
            var a by rememberSetting(setting)
            var b by rememberSetting(setting)

            assertEquals(a, b)
            Text(a.toString())
            LaunchedEffect(Unit) { a = TestEnum.C }
        }
        waitUntilText("C").assertIsDisplayed()
    }
}
