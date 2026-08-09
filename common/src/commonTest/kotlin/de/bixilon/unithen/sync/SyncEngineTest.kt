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

package de.bixilon.unithen.sync

import de.bixilon.unithen.storage.sql.dummy
import de.bixilon.unithen.test.UniThenTestOnly
import kotlinx.coroutines.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UniThenTestOnly::class)
class SyncEngineTest {

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    private fun use(block: suspend SyncEngineContext.() -> Unit): SyncEngineProgress? {
        val engine = SyncEngine(runBlocking { dummy() }) { throw it }
        var progress: SyncEngineProgress? = null
        runBlocking {
            coroutineScope {
                val context = SyncEngineContext(engine, true, this) { progress = it }
                block.invoke(context)
            }
        }

        return progress
    }

    private suspend fun SyncEngineContext.delay1() = test { delay(100.milliseconds) }

    @Test
    fun `initial progress`() {
        val progress = use { }

        assertNull(progress)
    }

    @Test
    fun `progress increase on noop progress`() {
        val progress = use { test { } }!!

        assertEquals(1, progress.completed)
        assertEquals(1, progress.total)
    }

    @Test
    fun `all async tests completed before return`() {
        val progress = use { async { delay1() }; async { delay1() } }!!

        assertEquals(2, progress.completed)
        assertEquals(2, progress.total)
    }
}
