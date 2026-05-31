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
import kotlin.reflect.KProperty1

data class SqlFilter(
    @param:Language("SQL") val sql: String,
    val parameters: List<Any> = emptyList(),
) {

    constructor(sql: String, vararg parameters: Any):this(sql, parameters.toList())

    private fun connect(@Language("SQL") conjunction: String, other: SqlFilter?): SqlFilter {
        if (other == null || other.sql.isBlank()) return this

        return SqlFilter("($sql) $conjunction (${other.sql})", parameters + other.parameters)
    }

    infix fun and(other: SqlFilter?) = connect("AND", other)
    infix fun or(other: SqlFilter?) = connect("OR", other)

    operator fun plus(other: String?) = if (other == null) this else SqlFilter(this.sql + " " + other, this.parameters)

    infix fun where(where: SqlFilter?) = if (where == null) this else SqlFilter(this.sql + " WHERE " + where.sql, this.parameters + where.parameters)



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
        fun comma(vararg filters: Pair<String, Any?>) = join(",", *filters)


        private fun <T> KProperty1<*, T>.create(operator: String, other: T): SqlFilter {
            return SqlFilter(this.name + operator + "?", listOf(other!!))
        }

        infix fun <T> KProperty1<*, T>.eq(other: T) = create("=", other)

        infix fun <T> KProperty1<*, T>.neq(other: T) = create("!=", other)
        infix fun <T> KProperty1<*, T>.gt(other: T) = create(">", other)
        infix fun <T> KProperty1<*, T>.ge(other: T) = create(">=", other)
        infix fun <T> KProperty1<*, T>.lt(other: T) = create("<", other)
        infix fun <T> KProperty1<*, T>.le(other: T) = create("<=", other)


        fun <T> KProperty1<*, T>.isNull() = SqlFilter(this.name + " IS NULL")
        fun <T> KProperty1<*, T>.isNotNull() = SqlFilter(this.name + " IS NOT NULL")
    }
}
