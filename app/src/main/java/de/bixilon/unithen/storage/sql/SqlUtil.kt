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

package de.bixilon.unithen.storage.sql

import android.database.Cursor
import androidx.core.database.getStringOrNull
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import java.util.*
import kotlin.time.Instant

object SqlUtil {

    fun Cursor.getUUID(index: Int) = getString(index).toUUID()
    fun Cursor.getUUIDOrNull(index: Int) = getStringOrNull(index)?.toUUID()
    fun Cursor.getInstant(index: Int) = Instant.fromEpochSeconds(getLong(index), 0)
    fun Cursor.getInstantOrNull(index: Int) = if (isNull(index)) null else getInstant(index)

    fun <T : Enum<T>> Cursor.getEnum(index: Int, values: ValuesEnum<T>) = values[getString(index)]

    fun Any?.db(): String? = when (this) {
        null -> null
        is Int -> this.toString()
        is Long -> this.toString()
        is String -> this
        is UUID -> this.toString()
        is Instant -> epochSeconds.toString()
        is Enum<*> -> name
        else -> throw IllegalArgumentException("Unknown parameter type: $this")
    }
}
