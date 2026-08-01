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

package de.bixilon.unithen.ui.util.state

import androidx.compose.runtime.*
import de.bixilon.kutil.time.minOf
import de.bixilon.unithen.ui.util.effects.RepeatedEffect
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource


private data class DelayedState<T>(
    val result: T,
    val start: TimeSource.Monotonic.ValueTimeMark,
    val last: TimeSource.Monotonic.ValueTimeMark,
)

@Composable
fun <T> rememberDelayedState(minimum: Duration, last: Duration, block: suspend (T) -> Unit): MutableState<T?> {
    var state by rememberStateOf<DelayedState<T>?> { null }

    RepeatedEffect(minOf(minimum, 100.milliseconds)) {
        val current = state ?: return@RepeatedEffect
        val now = TimeSource.Monotonic.markNow()
        if (now - current.start > minimum) {
            state = null
            if (now - current.last > last) {
                return@RepeatedEffect
            }
            block.invoke(current.result)
        }
    }

    return remember {
        object : MutableState<T?> {
            override var value: T?
                get() = state?.result
                set(value) {
                    val current = state
                    if (value == null) return
                    val now = TimeSource.Monotonic.markNow()
                    if (current == null || current.result != value) {
                        state = DelayedState(value, now, now)
                        return
                    }
                    state = current.copy(last = now)
                }

            override fun component1() = value

            override fun component2(): (T?) -> Unit = { value = it }
        }
    }
}
