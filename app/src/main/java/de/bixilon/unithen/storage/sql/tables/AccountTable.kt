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
import androidx.core.database.getStringOrNull
import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.api.user.UserDetails
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getInstant
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SelectableSqlTableSchema
import de.bixilon.unithen.storage.sql.util.SqlBuilder
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.eq
import de.bixilon.unithen.storage.sql.util.SqlTableSchema.Companion.column
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.storage.types.User
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTable(
    storage: SqlStorage,
) : SqlTable<Account>(storage, AccountTable) {

    operator fun get(id: Key) = single(AccountTable.id eq id)
    operator fun get(site: Site, user: User) = single(SqlFilter.and("site" to site.id, "user" to user.id))

    fun get(site: Site? = null,sessionKey: String? = null) = all(SqlFilter.and("site" to site,  "session_key" to sessionKey))
    fun update(id: Int, user: User?=null, sessionKey: String? = null, fetched: Instant? = null) = update(id, SqlFilter.comma("user" to user?.id, "session_key" to sessionKey, "fetched" to fetched))


    fun update(account: Account, user: User, authentication: Authentication) {
        update(account.id, user, authentication.cast<CookieAuthentication>().session)
    }

    fun insert(site: Site, user: User, authentication: Authentication): Account {
        val id = insert("INSERT INTO $table(site, user, session_key, fetched) VALUES (?,?,?,?)", site.id, user.id, authentication.cast<CookieAuthentication>().session, Clock.System.now())

        return this[id]!! // TODO: cleanup
    }

    fun add(site: Site, user: User, authentication: Authentication): Account {
        this[site, user]?.let { update(it, user, authentication); return this[it.id]!! }

        return insert(site, user, authentication)
    }

    operator fun get(course: Course) = all(select()
        .innerJoin(AccountCourses, AccountCourses.account eq id)
        .where(AccountCourses.course eq course.id)
    )

    fun getTutorAccount(course: Course): Account? {
        val query = SqlBuilder.select(AccountTable)
            .innerJoin(UserTable, (UserTable.uuid eq uuid) and (UserTable.site eq site))
            .innerJoin(AccountCourses, AccountCourses.account eq id)
            .innerJoin(TutorCourses, TutorCourses.user eq UserTable.id)
            .where(AccountCourses.course eq course.id)
            .and(TutorCourses.course eq course.id)
            .limit(1)

        return storage.query(query) { it.collectAll() }.firstOrNull()
    }

    fun getTutorAccount(appointment: Appointment): Account? {
        val course = storage.courses[appointment.course]!!
        getTutorAccount(course)?.let { return it }

        val query = SqlBuilder.select(AccountTable)
            .innerJoin(UserTable, (UserTable.uuid eq uuid) and (UserTable.site eq site))
            .innerJoin(AccountCourses, AccountCourses.account eq id)
            .innerJoin(CourseTable, AccountCourses.course eq CourseTable.id)
            .innerJoin(TutorAppointments, TutorAppointments.user eq UserTable.id)
            .where(AccountCourses.course eq appointment.course)
            .and(TutorAppointments.appointment eq appointment.id)
            .limit(1)

        return storage.query(query) { it.collectAll() }.firstOrNull()
    }

    fun logout(account: Account) {
        update(account.id, sessionKey = "")
    }


    fun clearCourses(account: Account) {
        insert("DELETE FROM account_courses WHERE account=?", account.id)
    }

    fun addToCourse(account: Account, course: Course) {
        insert("INSERT OR REPLACE INTO account_courses(account, course) VALUES (?,?)", account.id, course.id)
    }

    fun remove(account: Account) = storage.transaction {
        insert("DELETE FROM account_courses WHERE account=?", account.id)
        insert("DELETE FROM accounts WHERE id=?", account.id)
    }

    companion object : SelectableSqlTableSchema<Account> {
        override val table get() = "accounts"

        val id = column(Account::id)
        val site = column(Account::site)
        val user = column(Account::user)
        val sessionKey = column(Account::sessionKey)
        val fetched = column(Account::fetched)

        override val columns = listOf(id, site, user, sessionKey, fetched)

        override fun map(cursor: Cursor) = Account(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),  cursor.getStringOrNull(3), cursor.getInstant(4))
    }
}
