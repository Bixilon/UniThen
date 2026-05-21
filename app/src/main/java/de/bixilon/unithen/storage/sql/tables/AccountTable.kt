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

import android.database.Cursor
import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.api.user.UserDetails
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getInstant
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

class AccountTable(
    storage: SqlStorage,
) : SqlTable<Account>(storage, "accounts") {
    override val columns = listOf("id", "site", "uuid", "firstname", "lastname", "session_key", "fetched")

    override fun map(cursor: Cursor) = Account(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInstant(6))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(site: Site, uuid: UUID) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))

    fun get(site: Site? = null, uuid: UUID? = null, firstname: String? = null, lastname: String? = null, sessionKey: String? = null) = all(SqlFilter.and("site" to site, "uuid" to uuid, "firstname" to firstname, "lastname" to lastname, "session_key" to sessionKey))
    fun update(id: Int, firstname: String? = null, lastname: String? = null, sessionKey: String? = null, fetched: Instant? = null) = update(id, SqlFilter.comma("firstname" to firstname, "lastname" to lastname, "session_key" to sessionKey, "fetched" to fetched))


    fun update(account: Account, details: UserDetails, authentication: Authentication) {
        update(account.id, details.firstname, details.lastname, authentication.cast<CookieAuthentication>().session)
    }

    fun insert(site: Site, details: UserDetails, authentication: Authentication): Account {
        val id = insert("INSERT INTO $table(site, uuid, firstname, lastname, session_key, fetched) VALUES (?,?,?,?,?,?)", site.id, details.uuid, details.firstname, details.lastname, authentication.cast<CookieAuthentication>().session, Clock.System.now())

        return this[id]!! // TODO: cleanup
    }

    fun add(site: Site, details: UserDetails, authentication: Authentication): Account {
        this[site, details.uuid]?.let { update(it, details, authentication); return this[it.id]!! }

        return insert(site, details, authentication)
    }

    operator fun get(course: Course): List<Account> {
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table INNER JOIN account_courses ON account_courses.account = $table.id WHERE course = ?", course.id) { it.collectAll() }
    }

    fun logout(account: Account) {
        update(account.id, sessionKey = "")
    }

    fun addToCourse(account: Account, course: Course) {
        insert("INSERT INTO account_courses(account, course) VALUES (?,?) ON CONFLICT(account, course) DO NOTHING", account.id, course.id)
        notifyState()
    }
}
