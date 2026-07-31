package de.bixilon.unithen.ui

import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.RuntimeInfo.RuntimeInfo0
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AbstractComposeUiTest {

    init {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        RuntimeInfo0.actual = object : RuntimeInfo {
            override val debug get() = false
        }
    }
}
