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
import de.bixilon.kutil.time.Interval
import de.bixilon.unithen.sync.SyncEngineContext
import de.bixilon.unithen.ui.util.effects.RepeatedEffect
import kotlin.time.Duration


@Composable
fun useRepeatedSyncEngine(interval: Interval = Duration.INFINITE, block: suspend SyncEngineContext.() -> Unit): SyncEngineHook {
    val hook = useSyncEngine(block)

    RepeatedEffect(interval) {
        hook.invoke()
    }

    return hook
}
