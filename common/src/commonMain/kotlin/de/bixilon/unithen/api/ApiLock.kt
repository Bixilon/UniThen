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

package de.bixilon.unithen.api

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

const val MAX_PARALLEL_REQUEST = 10
const val MAX_PARALLEL_REQUESTS_HOST = 6

object ApiLock {
    val mutex = Mutex()
    val global = Semaphore(MAX_PARALLEL_REQUEST)
    val host: MutableMap<String, Semaphore> = mutableMapOf()


    suspend fun get(host: String): Semaphore {
        return mutex.withLock { this.host.getOrPut(host) { Semaphore(MAX_PARALLEL_REQUESTS_HOST) } }
    }

    suspend inline fun <T> withPermit(host: String, runnable: () -> T): T {
        return global.withPermit { get(host).withPermit(runnable) }
    }
}
