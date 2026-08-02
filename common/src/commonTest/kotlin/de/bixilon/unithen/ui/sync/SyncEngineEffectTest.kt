/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen.ui.sync

import androidx.compose.runtime.*
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.storage.sql.dummy
import de.bixilon.unithen.sync.SyncEngine
import de.bixilon.unithen.sync.SyncEngineContext
import de.bixilon.unithen.ui.AbstractComposeUiTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalTestApi::class)
class SyncEngineEffectTest : AbstractComposeUiTest() {

    @OptIn(InternalComposeApi::class)
    @Composable
    private fun useTestSyncEngine(block: suspend SyncEngineContext.() -> Unit): SyncEngineHook {
        val engine = remember { SyncEngine(runBlocking { dummy() }) { throw it } }

        currentComposer.startProvider(LocalSyncEngine provides engine)
        val hook = useSyncEngine(block)
        currentComposer.endProvider()

        return hook
    }

    @Test
    fun `create basic sync engine`() = runComposeUiTest {
        setContent {
            val synchronize = useTestSyncEngine { }

            assertFalse { synchronize.active }
        }
    }

    @Test
    fun `active when invoking`() = runComposeUiTest {
        val hook by leakState {
            useTestSyncEngine { delay(500.milliseconds) }
        }
        hook.invoke(force = true)

        waitUntil(150.milliseconds) { hook.active }
    }

    @Test
    fun `not firing when engine not active`() = runComposeUiTest {
        var fired = false
        val hook by leakState {
            val synchronize = useTestSyncEngine { delay(100.milliseconds) }

            SyncEngineStartedEffect(synchronize) { fired = true }
            SyncEngineCompleteEffect(synchronize) { fired = true }

            return@leakState synchronize
        }
        delay(10.milliseconds)
        assertFalse { fired }
    }

    @Test
    fun `start effect fires`() = runComposeUiTest {
        var fired = false
        val hook by leakState {
            val synchronize = useTestSyncEngine { delay(100.milliseconds) }

            SyncEngineStartedEffect(synchronize) { fired = true }

            return@leakState synchronize
        }
        hook.invoke(force = true)

        waitUntil(50.milliseconds) { fired }
    }

    @Test
    fun `complete effect fires`() = runComposeUiTest {
        var fired = false
        val hook by leakState {
            val synchronize = useTestSyncEngine { delay(100.milliseconds) }

            SyncEngineCompleteEffect(synchronize) { fired = true }

            return@leakState synchronize
        }
        hook.invoke(force = true)

        waitUntil(200.milliseconds) { fired }
    }

    private fun ComposeUiTest.waitUntil(timeout: Duration, block: () -> Boolean) {
        waitUntil("", timeout.inWholeMilliseconds, block)
    }
}
