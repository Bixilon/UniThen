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
import de.bixilon.kutil.exception.Unreachable
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.util.SqlFilter
import org.intellij.lang.annotations.Language

abstract class SqlTable<T>(
    protected val storage: SqlStorage,
    protected val table: String,
) {
    val count get() = storage.query("SELECT COUNT(*) FROM $table") { it.collectIntAggregation() }

    abstract val columns: List<String>

    protected abstract fun map(cursor: Cursor): T

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun get(): Nothing = Unreachable()

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun update(id: Int): Nothing = Unreachable()

    protected fun update(id: Key, filter: SqlFilter) {
        update("UPDATE $table SET ${filter.sql} WHERE id=?", parameters = arrayOf(*filter.parameters.toTypedArray(), id))
    }

    protected fun update(sql: String, vararg parameters: Any?) {
        storage.update(sql, parameters = parameters)
        storage.notifyState()
    }

    protected fun insert(@Language("SQL") sql: String, vararg parameters: Any?): Int {
        return storage.insert(sql, *parameters).apply { storage.notifyState() }
    }


    private fun <X> select(where: String = "", arguments: Array<out Any>, runnable: (Cursor) -> X): X {
        val actualWhere = if (where.isBlank()) "" else "WHERE $where"
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table $actualWhere", *arguments, runnable = runnable)
    }

    protected fun single(filter: SqlFilter) = single(filter.sql, arguments = filter.parameters.toTypedArray())
    protected fun single(@Language("SQL") where: String = "", vararg arguments: Any): T? {
        return select(where, arguments = arguments) {
            when (it.count) {
                0 -> null
                1 -> {
                    it.moveToNext()
                    map(it)
                }

                else -> throw IllegalStateException("More than one result found: $where")
            }
        }
    }

    protected fun first(filter: SqlFilter) = first(filter.sql, arguments = filter.parameters.toTypedArray())
    protected fun first(@Language("SQL") where: String = "", vararg arguments: Any): T? {
        return select(where, arguments = arguments) {
            when (it.count) {
                0 -> null
                else -> {
                    it.moveToNext()
                    map(it)
                }
            }
        }
    }


    protected fun Cursor.collectAll(): List<T> {
        val result = ArrayList<T>(count)

        while (moveToNext()) {
            result += map(this)
        }

        return result
    }

    protected fun Cursor.collectIntAggregation(): Int {
        moveToFirst(); return getInt(0)
    }

    protected fun all(filter: SqlFilter) = all(filter.sql, *filter.parameters.toTypedArray())
    protected fun all(@Language("SQL") where: String = "", vararg arguments: Any): List<T> {
        return select(where, arguments = arguments, runnable = { it.collectAll() })
    }
    fun all(): List<T> = all("TRUE")
}
