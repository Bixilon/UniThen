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
        infix fun from(table: SqlTable<*>) = From(fields, table.columns)
    }

    class From internal constructor(
        val fields: List<String>,
        val from: List<String>,
    ) : Executable {

        override fun toSql() = SqlStatement("SELECT ${fields.joinToString(",")} FROM ${from.joinToString(",")}", listOf()) // TODO: SQL injection possible?

        infix fun where(filter: SqlFilter?) = if (filter == null) this else Where(this, filter)
        infix fun where(filter: SqlFilter) = Where(this, filter)
    }

    class Where internal constructor(
        val executable: Executable,
        val where: SqlFilter,
    ) : Executable {

        override fun toSql() = executable + SqlStatement("WHERE (" + where.sql + ")", where.parameters)

        infix fun and(filter: SqlFilter?) = if (filter == null) this else Where(executable, this.where and filter)
        infix fun or(filter: SqlFilter?) = if (filter == null) this else Where(executable, this.where or filter)

        fun order(field: String, sort: Order.Sort = Order.Sort.ASC) = Order(this, listOf(field), sort)
        fun order(vararg fields: String, sort: Order.Sort = Order.Sort.ASC) = Order(this, fields.toList(), sort)
    }

    class Order internal constructor(
        val executable: Executable,
        val fields: List<String>,
        val sort: Sort,
    ) : Executable {

        override fun toSql() = executable + SqlStatement("ORDER BY " + fields.joinToString(",") + " " + sort.name, listOf())

        enum class Sort {
            ASC,
            DESC,
        }
    }


    fun select(vararg fields: String) = Select(fields = fields.toList())
    fun select(count: Aggregations.Count) = select("COUNT(*)")
    // inline fun <reified T : DbObject> select() = select(TODO()) // TODO
}
