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

data class SqlFilter(
    val where: String,
    val parameters: List<Any>,
) {

    companion object {
        val EMPTY = SqlFilter("", emptyList())

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
            if (parameters.isEmpty()) return SqlFilter.EMPTY

            return SqlFilter("$string", parameters)
        }

        fun and(vararg filters: Pair<String, Any?>) = join(" AND ", *filters)
        fun or(vararg filters: Pair<String, Any?>) = join(" OR ", *filters)
        fun comma(vararg filters: Pair<String, Any?>) = join(",", *filters)
    }
}
