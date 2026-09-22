package de.bixilon.unithen.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.main.MainRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.util.state.rememberDelayedState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalTestApi::class)
class DelayedStateTest : AbstractComposeUiTest() {

    @OptIn(InternalComposeApi::class)
    @Composable
    fun state(block: suspend (String) -> Unit): MutableState<String?> {
        currentComposer.startProvider(LocalNavigation provides Navigator(MainRoute))
        val state = rememberDelayedState(200.milliseconds, 50.milliseconds, block)
        currentComposer.endProvider()

        return state
    }


    @Test
    fun `initially empty`() = runComposeUiTest {
        var triggered: String? = null
        val state = leak { state { triggered = it } }

        assertEquals(null, state.value)
        assertNull(triggered)
    }

    @Test
    fun `state set after calling set`() = runComposeUiTest {
        var triggered: String? = null
        val state = leak { state { triggered = it } }
        state.value = "A"
        assertEquals("A", state.value)
        assertNull(triggered)
    }

    // @Test
    fun `state cleared without triggering`() = runComposeUiTest {
        mainClock.autoAdvance = false
        var triggered: String? = null
        val state = leak { state { triggered = it } }
        state.value = "A"

        mainClock.advanceTimeBy(300)

        assertEquals(null, state.value)
        assertNull(triggered)
    }

    // @Test
    fun `state cleared with triggering`() = runComposeUiTest {
        mainClock.autoAdvance = false
        var triggered: String? = null
        val state = leak { state { triggered = it } }
        state.value = "A"

        mainClock.advanceTimeBy(100)
        state.value = "A"
        mainClock.advanceTimeBy(100)
        state.value = "A"
        mainClock.advanceTimeBy(100)
        state.value = "A"
        mainClock.advanceTimeBy(100)

        assertEquals(null, state.value)
        assertEquals("A", triggered)
    }
}
