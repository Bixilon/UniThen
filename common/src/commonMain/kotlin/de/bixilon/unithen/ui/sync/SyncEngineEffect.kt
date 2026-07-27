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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.bixilon.unithen.sync.SyncEngineContext
import de.bixilon.unithen.sync.SyncEngineProgress
import de.bixilon.unithen.ui.util.state.rememberStateOf
import kotlinx.coroutines.launch


@Composable
fun useSyncEngine(block: suspend SyncEngineContext.() -> Unit): SyncEngineHook {
    val engine = LocalSyncEngine.current
    var active by rememberStateOf { false }
    var progress by rememberStateOf<SyncEngineProgress?> { null }

    val sync = remember {
        val invokeable: SyncEngineInvoker = { force ->
            active = true
            val context = SyncEngineContext(engine, force) { progress = it }

            context.scope.launch {
                try {
                    block.invoke(context)
                } finally {
                    active = false
                    progress = null
                }
            }

        }

        return@remember invokeable
    }


    return SyncEngineHook(active, progress, sync)
}
