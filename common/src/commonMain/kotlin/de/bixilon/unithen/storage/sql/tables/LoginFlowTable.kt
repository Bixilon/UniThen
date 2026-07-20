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

package de.bixilon.unithen.storage.sql.tables

import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.SQLiteHelper
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.util.SelectableSqlTableSchema
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.eq
import de.bixilon.unithen.storage.sql.util.SqlTableSchema.Companion.column
import de.bixilon.unithen.storage.types.LoginFlow
import de.bixilon.unithen.storage.types.Site
import kotlin.time.Instant

class LoginFlowTable(
    storage: SqlStorage,
) : SqlTable<LoginFlow>(storage, LoginFlowTable) {

    operator fun get(id: Key) = single(LoginFlowTable.id eq id)


    fun create(site: Site, expires: Instant): LoginFlow {
        // TODO: Limit table size

        return insert(site, null, expires)
    }

    fun update(id: Key, exchangeToken: String? = null) = update(id, SqlFilter.comma("exchange_token" to exchangeToken))

    fun insert(site: Site, exchangeToken: String?, expires: Instant): LoginFlow {
        val id = insert("INSERT INTO $table(site, exchange_token, expires) VALUES (?,?,?)", site.id, exchangeToken, expires)

        return this[id]!!
    }

    fun delete(id: Key) = execute("DELETE FROM ${Companion.table} WHERE id=?", id)


    companion object : SelectableSqlTableSchema<LoginFlow> {
        override val table get() = "login_flows"
        const val MAX = 100

        val id = column(LoginFlow::id)
        val site = column(LoginFlow::site)
        val exchangeToken = column(LoginFlow::exchangeToken)
        val expires = column(LoginFlow::expires)

        override val columns = listOf(id, site, exchangeToken, expires)

        override fun map(cursor: SQLiteHelper.Cursor) = LoginFlow(cursor.getInt(0), cursor.getInt(1), cursor.getStringOrNull(2), cursor.getInstant(3))
    }
}
