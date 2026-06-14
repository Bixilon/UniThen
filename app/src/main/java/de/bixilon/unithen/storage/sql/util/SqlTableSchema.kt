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

import de.bixilon.kutil.string.StringUtil.toSnakeCase
import de.bixilon.unithen.storage.DbObject
import kotlin.reflect.KProperty1

interface SqlTableSchema<T : DbObject> {
    val table: String


    class SqlColumn<T>(val table: String, val name: String) {
        val quantifier get() = "$table.$name"

        override fun toString() = quantifier
    }


    companion object {
        inline fun <S : DbObject, V> SqlTableSchema<S>.column(field: KProperty1<S, V>) = SqlColumn<V>(this.table, field.name.toSnakeCase())
    }
}
