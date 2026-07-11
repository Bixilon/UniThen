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

import de.bixilon.kutil.exception.Unreachable
import de.bixilon.unithen.storage.DbObject
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.util.SelectableSqlTableSchema
import de.bixilon.unithen.storage.sql.util.SqlBuilder
import de.bixilon.unithen.storage.sql.util.SqlFilter
import org.intellij.lang.annotations.Language

abstract class SqlTable<T : DbObject>(
    protected val storage: SqlStorage,
    val schema: SelectableSqlTableSchema<T>,
) {
    @Deprecated("schema")
    val table get() = schema.table

    @Deprecated("schema")
    val columns = schema.columns.map { it.quantifier }
    val count get() = select(SqlBuilder.select(SqlBuilder.Aggregations.Count).from(this.schema)) { it.collectIntAggregation() }

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun get(): Nothing = Unreachable()

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun update(id: Int): Nothing = Unreachable()

    protected fun update(id: Key, filter: SqlFilter) {
        update("UPDATE $table SET ${filter.sql} WHERE id=?", parameters = arrayOf(*filter.parameters.toTypedArray(), id))
    }

    protected fun update(@Language("SQL") sql: String, vararg parameters: Any?) {
        storage.update(sql, parameters = parameters)
    }

    protected fun insert(@Language("SQL") sql: String, vararg parameters: Any?): Int {
        return storage.insert(sql, *parameters).toInt() // TODO: That is bad, it is returning the row id, not the id
    }

    protected fun single(filter: SqlFilter) = single(SqlBuilder.select(schema).where(filter))

    protected fun first(filter: SqlFilter) = first(SqlBuilder.select(schema).where(filter))

    private fun <X> select(query: SqlBuilder.Executable, runnable: (SQLiteHelper.Cursor) -> X): X {
        return storage.query(query, runnable)
    }

    protected fun select() = SqlBuilder.select(schema)

    fun single(query: SqlBuilder.Executable) = select(query) {
        if (!it.moveToNext()) return@select null
        val value = schema.map(it)
        if (it.moveToNext()) {
            throw IllegalStateException("More than one result found: $query")
        }
        return@select value
    }

    fun first(query: SqlBuilder.Executable) = select(query) {
        if (!it.moveToNext()) return@select null
        return@select schema.map(it)
    }

    protected fun all(query: SqlBuilder.Executable): List<T> {
        return select(query) { it.collectAll() }
    }

    protected fun SQLiteHelper.Cursor.collectAll(): List<T> {
        val result = ArrayList<T>()

        while (moveToNext()) {
            result += schema.map(this)
        }

        return result
    }


    protected fun all(filter: SqlFilter) = all(select().where(filter))

    fun all(): List<T> = all(select())
}
