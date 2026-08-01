package de.bixilon.unithen.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.RuntimeInfo.RuntimeInfo0
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
abstract class AbstractComposeUiTest {

    init {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        RuntimeInfo0.actual = object : RuntimeInfo {
            override val debug get() = false
        }
    }

    fun <T> ComposeUiTest.leakState(block: @Composable () -> T): MutableState<T> {
        val state = mutableStateOf<T?>(null)
        setContent {
            state.value = block.invoke()
        }

        waitUntil { state.value != null }

        return state.cast()
    }

    fun <T> ComposeUiTest.leak(block: @Composable () -> T) = leakState(block).value
}
