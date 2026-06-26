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

package de.bixilon.unithen.ui.storage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.ui.util.rememberAsync

val LocalStorage = staticCompositionLocalOf<SqlStorage> { throw IllegalStateException("No storage set!") }


@Composable
fun <T> rememberStorage(block: SqlStorage.() -> T): T {
    val storage = LocalStorage.current
    val value = remember(storage.notify.intValue) { block.invoke(storage) }


    return value
}

@Composable
fun <T> rememberStorageAsync(block: SqlStorage.() -> T): T? {
    val storage = LocalStorage.current
    val value = rememberAsync(storage.notify.intValue) { block.invoke(storage) }


    return value
}

@Composable
fun <T> rememberStorageAsync(key: Any?, block: SqlStorage.() -> T): T? {
    val storage = LocalStorage.current
    val value = rememberAsync(storage.notify.intValue, key) { block.invoke(storage) }


    return value
}


@Composable
fun <T> rememberStorageAsync(vararg keys: Any?, block: SqlStorage.() -> T): T? {
    val storage = LocalStorage.current
    val value = rememberAsync(storage.notify.intValue, *keys) { block.invoke(storage) }


    return value
}
