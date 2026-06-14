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
import de.bixilon.unithen.storage.DbObject
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.util.SqlBuilder
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.sql.util.SqlTableSchema
import org.intellij.lang.annotations.Language

abstract class SqlTable<T : DbObject>(
    protected val storage: SqlStorage,
    val schema: SqlTableSchema<T>,
) {
    val table get() = schema.table
    val columns = schema.columns.map { it.quantifier }
    val count get() = storage.query("SELECT COUNT(*) FROM $table") { it.collectIntAggregation() }

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun get(): Nothing = Unreachable()

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun update(id: Int): Nothing = Unreachable()

    protected fun update(id: Key, filter: SqlFilter) {
        update("UPDATE $table SET ${filter.sql} WHERE id=?", parameters = arrayOf(*filter.parameters.toTypedArray(), id))
    }

    protected fun update(sql: String, vararg parameters: Any?) {
        storage.update(sql, parameters = parameters)
    }

    protected fun insert(@Language("SQL") sql: String, vararg parameters: Any?): Int {
        return storage.insert(sql, *parameters)
    }


    @Deprecated("SqlQuery")
    private fun <X> select(where: String = "", arguments: Array<out Any>, runnable: (Cursor) -> X): X {
        val actualWhere = if (where.isBlank()) "" else "WHERE $where"
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table $actualWhere", *arguments, runnable = runnable)
    }

    @Deprecated("SqlQuery")
    protected fun single(filter: SqlFilter) = single(filter.sql, arguments = filter.parameters.toTypedArray())

    @Deprecated("SqlQuery")
    protected fun single(@Language("SQL") where: String = "", vararg arguments: Any): T? {
        return select(where, arguments = arguments) {
            if (!it.moveToNext()) return@select null
            val value = schema.map(it)
            if (it.moveToNext()) {
                throw IllegalStateException("More than one result found: $where")
            }
            return@select value
        }
    }

    @Deprecated("SqlQuery")
    protected fun first(filter: SqlFilter) = first(filter.sql, arguments = filter.parameters.toTypedArray())

    @Deprecated("SqlQuery")
    protected fun first(@Language("SQL") where: String = "", vararg arguments: Any): T? {
        return select(where, arguments = arguments) {
            if (!it.moveToNext()) return@select null
            return@select schema.map(it)
        }
    }

    fun first(query: SqlBuilder.Executable): T? {
        return storage.query(query) {
            if (!it.moveToNext()) return@query null
            return@query schema.map(it)
        }
    }


    protected fun Cursor.collectAll(): List<T> {
        val result = ArrayList<T>()

        while (moveToNext()) {
            result += schema.map(this)
        }

        return result
    }

    protected fun Cursor.collectIntAggregation(): Int {
        moveToNext(); return getInt(0)
    }

    protected fun Cursor.isEmpty(): Boolean {
        if (!moveToNext()) return true

        moveToPrevious()
        return false
    }

    protected fun Cursor.isNotEmpty() = !isEmpty()

    @Deprecated("SqlQuery")
    protected fun all(filter: SqlFilter) = all(filter.sql, *filter.parameters.toTypedArray())

    @Deprecated("SqlQuery")
    protected fun all(@Language("SQL") where: String = "", vararg arguments: Any): List<T> {
        return select(where, arguments = arguments, runnable = { it.collectAll() })
    }

    fun all(): List<T> = all("1")
}
