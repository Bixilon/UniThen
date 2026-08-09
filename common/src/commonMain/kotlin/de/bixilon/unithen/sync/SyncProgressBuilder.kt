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

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

@OptIn(ExperimentalAtomicApi::class)
class SyncProgressBuilder(
    val callback: (SyncEngineProgress) -> Unit,
    total: Int = 0,
) {
    private val completed = AtomicInt(0)
    private val synchronized = AtomicInt(0)
    private val warning = AtomicInt(0)
    private val errored = AtomicInt(0)
    private val total = AtomicInt(total)

    private fun call() {
        val progress = SyncEngineProgress(completed.load(), synchronized.load(), warning.load(), errored.load(), total.load())
        callback.invoke(progress)
    }

    private inline fun called(block: () -> Unit) {
        block.invoke()
        call()
    }

    private fun increment(int: AtomicInt) = called { int.incrementAndFetch() }

    fun addTotal() = increment(total)

    fun addTotal(count: Int) = called { total.addAndFetch(count) }

    fun addComplete() = called { completed.incrementAndFetch(); synchronized.incrementAndFetch() }
    fun addSkipped() = increment(completed)
    fun addWarning() = increment(warning)
    fun addError() = increment(errored)
}
