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

import de.bixilon.kutil.enums.ValuesEnum
import java.io.Closeable
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface SQLiteHelper : Closeable {

    fun load()

    fun query(sql: String, vararg parameters: Any?): Cursor
    fun execute(sql: String, vararg parameters: Any?): Int
    fun insert(sql: String, vararg parameters: Any?): Int

    fun executeBatch(path: String) {
        val statements = SqlUtil.split(SqlUtil.load(path))
        transaction { statements.forEach { execute(it) } }
    }

    fun <T> transaction(block: () -> T): T


    interface Cursor : Closeable {
        fun getBlob(index: Int): ByteArray
        fun getBlobOrNull(index: Int): ByteArray?

        fun getString(index: Int): String
        fun getStringOrNull(index: Int): String?

        fun getInt(index: Int): Int
        fun getLong(index: Int): Long

        fun isNull(index: Int): Boolean


        fun moveToNext(): Boolean

        fun getUUID(index: Int) = getString(index).let { Uuid.parse(it) }
        fun getUUIDOrNull(index: Int) = getStringOrNull(index)?.let { Uuid.parse(it) }
        fun getInstant(index: Int) = Instant.fromEpochSeconds(getLong(index), 0)
        fun getInstantOrNull(index: Int) = if (isNull(index)) null else getInstant(index)

        fun <T : Enum<T>> getEnum(index: Int, values: ValuesEnum<T>) = values[getString(index)]


        fun collectIntAggregation(): Int {
            moveToNext(); return getInt(0)
        }

        fun isEmpty(): Boolean {
            return !moveToNext()
        }

        fun isNotEmpty() = !isEmpty()
    }
}
