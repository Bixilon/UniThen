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

import org.intellij.lang.annotations.Language

data class SqlFilter(
    @param:Language("SQL") val sql: String,
    val parameters: List<Any> = emptyList(),
) {

    constructor(sql: String, vararg parameters: Any) : this(sql, parameters.toList())

    private fun connect(@Language("SQL") conjunction: String, other: SqlFilter?): SqlFilter {
        if (other == null || other.sql.isBlank()) return this

        return SqlFilter("($sql) $conjunction (${other.sql})", parameters + other.parameters)
    }

    infix fun and(other: SqlFilter?) = connect("AND", other)
    infix fun or(other: SqlFilter?) = connect("OR", other)

    infix fun exists(query: SqlBuilder.Executable) = query.toSql().let { SqlFilter("EXISTS (${it.sql})", parameters + it.parameters) }

    fun not() = SqlFilter("NOT ($sql)", parameters)


    companion object {
        val EMPTY = SqlFilter("", emptyList())


        operator fun invoke() = EMPTY

        fun join(separator: String, vararg filters: Pair<String, Any?>): SqlFilter {
            val parameters = ArrayList<Any>()
            val string = StringBuilder()

            for ((argument, value) in filters) {
                if (value == null) continue
                assert("\"" !in argument)
                assert("'" !in argument)

                if (string.isNotEmpty()) {
                    string.append(separator)
                }
                string.append(argument).append("=?")

                parameters += value
            }
            if (parameters.isEmpty()) return EMPTY

            return SqlFilter("$string", parameters)
        }

        fun and(vararg filters: Pair<String, Any?>) = join(" AND ", *filters)
        fun or(vararg filters: Pair<String, Any?>) = join(" OR ", *filters)
        fun exists(query: SqlBuilder.Executable) = query.toSql().let { SqlFilter("EXISTS (${it.sql})", it.parameters) }
        fun contains(field: SqlTableSchema.SqlColumn<*>, query: SqlBuilder.Executable) = query.toSql().let { SqlFilter("${field.quantifier} IN (${it.sql})", it.parameters) }

        fun comma(vararg filters: Pair<String, Any?>) = join(",", *filters)


        private fun <T> SqlTableSchema.SqlColumn<T>.create(operator: String, other: T): SqlFilter {
            return SqlFilter(this.quantifier + operator + "?", listOf(other!!))
        }

        private fun <T> SqlTableSchema.SqlColumn<T>.create(operator: String, other: SqlTableSchema.SqlColumn<T>): SqlFilter {
            return SqlFilter(this.quantifier + operator + other.quantifier)
        }

        infix fun <T> SqlTableSchema.SqlColumn<T>.eq(other: T) = create("=", other)
        infix fun <T> SqlTableSchema.SqlColumn<T>.eq(other: SqlTableSchema.SqlColumn<T>) = create("=", other)

        infix fun <T> SqlTableSchema.SqlColumn<T>.neq(other: T) = create("!=", other)
        infix fun <T> SqlTableSchema.SqlColumn<T>.gt(other: T) = create(">", other)
        infix fun <T> SqlTableSchema.SqlColumn<T>.ge(other: T) = create(">=", other)
        infix fun <T> SqlTableSchema.SqlColumn<T>.lt(other: T) = create("<", other)
        infix fun <T> SqlTableSchema.SqlColumn<T>.le(other: T) = create("<=", other)


        fun <T> SqlTableSchema.SqlColumn<T?>.isNull() = SqlFilter(this.quantifier + " IS NULL")
        fun <T> SqlTableSchema.SqlColumn<T?>.isNotNull() = SqlFilter(this.quantifier + " IS NOT NULL")
    }
}
