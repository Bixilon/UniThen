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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import de.bixilon.kutil.time.Interval
import de.bixilon.unithen.sync.SyncEngine
import de.bixilon.unithen.sync.status.SyncProgressUpdate
import de.bixilon.unithen.sync.status.SyncStatusUpdate
import de.bixilon.unithen.ui.util.effects.RepeatedEffect
import de.bixilon.unithen.ui.util.state.rememberStateOf
import kotlin.time.Duration

data class SyncEngineReport(
    val completed: Int,
    val errored: Int,
    val warnings: Int,
    val total: Int,
)

@Composable
fun useSyncEngine(interval: Interval = Duration.INFINITE, block: suspend SyncEngine.(reporter: (SyncStatusUpdate) -> Unit) -> Unit): SyncEngineReport? {
    val engine = LocalSyncEngine.current
    var state by rememberStateOf<SyncEngineReport?> { null }

    // TODO: That does only work with a single call, port to SyncEngineContext
    RepeatedEffect(interval) {
        val callback: (it: SyncStatusUpdate) -> Unit = {
            when (it) {
                is SyncProgressUpdate -> state = SyncEngineReport(it.completed, it.errored, it.warnings, it.total)
            }
        }

        block.invoke(engine, callback)

        state = null
    }

    return state
}
