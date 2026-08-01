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

import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.user.UserDetails
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.SQLiteHelper
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.util.SelectableSqlTableSchema
import de.bixilon.unithen.storage.sql.util.SqlBuilder
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.eq
import de.bixilon.unithen.storage.sql.util.SqlTableSchema.Companion.column
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.Site
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTable(
    storage: SqlStorage,
) : SqlTable<Account>(storage, AccountTable) {

    operator fun get(id: Key) = single(AccountTable.id eq id)
    operator fun get(site: Site, uuid: Uuid) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))

    fun get(site: Site? = null, uuid: Uuid? = null, firstname: String? = null, lastname: String? = null, sessionKey: String? = null) = all(SqlFilter.and("site" to site, "uuid" to uuid, "firstname" to firstname, "lastname" to lastname, "session_key" to sessionKey))
    fun update(id: Int, firstname: String? = null, lastname: String? = null, sessionKey: String? = null, fetched: Instant? = null) = update(id, SqlFilter.comma("firstname" to firstname, "lastname" to lastname, "session_key" to sessionKey, "fetched" to fetched))


    fun update(account: Account, details: UserDetails, authentication: Authentication) {
        update(account.id, details.firstname, details.lastname, authentication.token)
    }

    fun insert(site: Site, details: UserDetails, authentication: Authentication): Account {
        val id = insert(AccountTable, AccountTable.site to site.id, uuid to details.uuid, firstname to details.firstname, lastname to details.lastname, sessionKey to authentication.token, fetched to Instant.DISTANT_PAST)

        return this[id]!! // TODO: cleanup
    }

    fun add(site: Site, details: UserDetails, authentication: Authentication): Account {
        this[site, details.uuid]?.let { update(it, details, authentication); return this[it.id]!! }

        return insert(site, details, authentication)
    }

    operator fun get(course: Course) = all(select()
        .innerJoin(AccountCourses, AccountCourses.account eq id)
        .where(AccountCourses.course eq course.id)
    )

    fun getTutorAccount(course: Course): Account? {
        val query = SqlBuilder.select(AccountTable)
            .innerJoin(AccountCourses, AccountCourses.account eq id)
            .where((AccountCourses.tutor eq true) and (AccountCourses.course eq course.id))
            .limit(1)

        return storage.query(query) { it.first() }
    }

    fun getTutorAccount(appointment: Appointment): Account? {
        val course = storage.courses[appointment.course]!!

        return getTutorAccount(course)
    }

    fun logout(account: Account) {
        update(account.id, sessionKey = "")
    }


    fun clearCourses(account: Account) {
        execute("DELETE FROM account_courses WHERE account=?", account.id)
    }

    fun addToCourse(account: Account, course: Course, tutor: Boolean) {
        execute("INSERT OR REPLACE INTO account_courses(account, course, tutor) VALUES (?,?,?)", account.id, course.id, tutor)
    }

    fun remove(account: Account) = storage.transaction {
        execute("DELETE FROM account_courses WHERE account=?", account.id)
        execute("DELETE FROM accounts WHERE id=?", account.id)
    }

    fun isTutor(account: Account, course: Course): Boolean {
        val query = SqlBuilder.select("1").from(AccountCourses)
            .where((AccountCourses.account eq account.id) and (AccountCourses.course eq course.id) and (AccountCourses.tutor eq true))
            .limit(1)

        return storage.query(query) { it.isNotEmpty() }
    }

    fun isTutor(course: Course): Boolean {
        val query = SqlBuilder.select("1").from(AccountCourses)
            .where((AccountCourses.course eq course.id) and (AccountCourses.tutor eq true))
            .limit(1)

        return storage.query(query) { it.isNotEmpty() }
    }

    companion object : SelectableSqlTableSchema<Account> {
        override val table get() = "accounts"

        val id = column(Account::id)
        val site = column(Account::site)
        val uuid = column(Account::uuid)
        val firstname = column(Account::firstname)
        val lastname = column(Account::lastname)
        val sessionKey = column(Account::sessionKey)
        val fetched = column(Account::fetched)

        override val columns = listOf(id, site, uuid, firstname, lastname, sessionKey, fetched)

        override fun map(cursor: SQLiteHelper.Cursor) = Account(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getString(3), cursor.getString(4), cursor.getStringOrNull(5), cursor.getInstant(6))
    }
}
