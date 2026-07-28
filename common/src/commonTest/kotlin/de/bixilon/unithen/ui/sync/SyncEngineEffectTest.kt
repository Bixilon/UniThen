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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.storage.sql.dummy
import de.bixilon.unithen.sync.SyncEngine
import de.bixilon.unithen.sync.SyncEngineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalTestApi::class)
class SyncEngineEffectTest {

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
        setContent {
            val synchronize = useTestSyncEngine { delay(100.milliseconds) }

            assertTrue { synchronize.active }
        }
    }

    @Test
    fun `start effect fires`() = runComposeUiTest {
        var fired = false
        setContent {
            val synchronize = useTestSyncEngine { delay(100.milliseconds) }

            SyncEngineStartedEffect(synchronize) { fired = true }
        }
        assertTrue(fired)
    }

    @Test
    fun `complete effect fires`() = runComposeUiTest {
        var fired = false
        setContent {
            val synchronize = useTestSyncEngine { delay(100.milliseconds) }

            SyncEngineCompleteEffect(synchronize) { fired = true }
            waitUntil { !synchronize.active }
        }
        assertTrue(fired)
    }
}
