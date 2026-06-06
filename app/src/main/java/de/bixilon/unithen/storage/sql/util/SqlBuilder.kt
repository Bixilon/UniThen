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

package de.bixilon.unithen.storage.sql.util

import de.bixilon.unithen.storage.sql.SqlTable

object SqlBuilder {

    object Aggregations {
        object Count
    }

    data class SqlStatement(val sql: String, val parameters: List<Any>) : Executable {
        override fun toSql() = this
    }

    interface Executable {
        fun toSql(): SqlStatement

        operator fun plus(other: Executable): SqlStatement {
            val a = toSql()
            val b = other.toSql()

            return SqlStatement(a.sql + " " + b.sql, a.parameters + b.parameters)
        }
    }

    class Select internal constructor(val fields: List<String>) {

        infix fun from(table: String) = From(fields, listOf(table))
        infix fun from(table: SqlTable<*>) = from(table.table)
    }

    interface Joinable : Executable {
        fun innerJoin(table: String, on: SqlFilter) = InnerJoin(this, table, on)
        fun innerJoin(table: String, on: String) = InnerJoin(this, table, SqlFilter(on, listOf()))

        fun leftJoin(table: String, on: SqlFilter) = LeftJoin(this, table, on)
        fun leftJoin(table: String, on: String) = LeftJoin(this, table, SqlFilter(on, listOf()))
    }

    class From internal constructor(
        private val fields: List<String>,
        private val from: List<String>,
    ) : Whereable, Executable, Joinable, Orderable, Limitable {

        override fun toSql() = SqlStatement("SELECT ${fields.joinToString(",")} FROM ${from.joinToString(",")}", listOf()) // TODO: SQL injection possible?
    }

    class InnerJoin internal constructor(
        private val executable: Executable,
        private val table: String,
        private val on: SqlFilter,
    ) : Whereable, Executable, Joinable, Orderable, Limitable {
        override fun toSql() = executable + SqlStatement("INNER JOIN $table ON", listOf()) + SqlStatement(on.sql, on.parameters)
    }

    class LeftJoin internal constructor(
        private val executable: Executable,
        private val table: String,
        private val on: SqlFilter,
    ) : Whereable, Executable, Joinable, Orderable, Limitable {
        override fun toSql() = executable + SqlStatement("LEFT JOIN $table ON", listOf()) + SqlStatement(on.sql, on.parameters)
    }

    interface Whereable : Executable {
        infix fun where(filter: SqlFilter?) = if (filter == null) this else Where(this, filter)
        infix fun where(filter: SqlFilter) = Where(this, filter)
    }

    class Where internal constructor(
        private val executable: Executable,
        private val where: SqlFilter,
    ) : Executable, Orderable, Limitable {

        override fun toSql() = executable + SqlStatement("WHERE (" + where.sql + ")", where.parameters)

        infix fun and(filter: SqlFilter?) = if (filter == null) this else Where(executable, this.where and filter)
        infix fun or(filter: SqlFilter?) = if (filter == null) this else Where(executable, this.where or filter)
    }

    interface Orderable : Executable {
        fun order(field: String, order: Order.Order = Order.Order.ASC) = order(field to order)
        fun order(vararg fields: Pair<String, Order.Order>) = Order(this, fields.toList())
    }

    class Order internal constructor(
        private val executable: Executable,
        private val sort: List<Pair<String, Order>>,
    ) : Executable, Limitable {

        override fun toSql() = executable + SqlStatement("ORDER BY " + sort.joinToString(",") { it.first + " " + it.second.name }, listOf())

        enum class Order {
            ASC,
            DESC,
        }
    }

    interface Limitable : Executable {
        fun limit(count: Int) = Limit(this, count)
    }

    class Limit internal constructor(
        private val executable: Executable,
        private val limit: Int,
    ) : Executable {
        override fun toSql() = executable + SqlStatement("LIMIT ?", listOf(limit))

        init {
            assert(limit > 0)
        }
    }


    fun select(vararg fields: String) = Select(fields = fields.toList())
    fun select(count: Aggregations.Count) = select("COUNT(*)")

    fun select(schema: SqlTableSchema<*>) = Select(schema.columns.map { it.quantifier }) from schema.table
}
