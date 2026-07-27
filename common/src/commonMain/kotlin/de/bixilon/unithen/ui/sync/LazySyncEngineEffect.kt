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
import de.bixilon.unithen.sync.SyncEngineContext
import de.bixilon.unithen.ui.util.state.rememberStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

private typealias SyncEngineInvoker = (force: Boolean) -> Unit

data class LazySyncEngineHook(
    val active: Boolean,
    private val invoke: SyncEngineInvoker,
) {

    operator fun invoke(force: Boolean = false) = this.invoke.invoke(false)
}


@Composable
fun useLazySyncEngine(block: suspend SyncEngineContext.() -> Unit): LazySyncEngineHook {
    val engine = LocalSyncEngine.current
    val active by rememberStateOf { false }

    val sync = remember {
        val invokeable: SyncEngineInvoker = { force ->
            val scope = CoroutineScope(Dispatchers.IO)
            val context = SyncEngineContext(engine, force, scope)

            scope.launch { block.invoke(context) }
        }

        return@remember invokeable
    }


    return LazySyncEngineHook(active, sync)
}
