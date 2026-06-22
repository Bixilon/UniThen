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
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getInstant
import de.bixilon.unithen.storage.sql.SqlUtil.getInstantOrNull
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SelectableSqlTableSchema
import de.bixilon.unithen.storage.sql.util.SqlBuilder
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.eq
import de.bixilon.unithen.storage.sql.util.SqlTableSchema.Companion.column
import de.bixilon.unithen.storage.types.*
import kotlin.time.Instant
import kotlin.uuid.Uuid

class CourseTable(
    storage: SqlStorage,
) : SqlTable<Course>(storage, CourseTable) {
    operator fun get(id: Key) = single(CourseTable.id eq id)
    operator fun get(site: Site, uuid: Uuid) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))

    fun get(site: Site? = null, event: Event? = null, uuid: Uuid? = null, name: String? = null) = all(SqlFilter.and("site" to site?.id, "event" to event?.id, "uuid" to uuid, "name" to name))

    fun update(id: Key, name: String? = null, fetched: Instant? = null, fetchedEnrolled: Instant? = null) = update(id, SqlFilter.comma("name" to name, "fetched" to fetched, "fetched_enrolled" to fetchedEnrolled))


    fun insert(site: Site, event: Event, uuid: Uuid, name: String, fetched: Instant): Course {
        val id = insert("INSERT INTO $table(site, event, uuid, name, fetched) VALUES (?,?,?,?,?)", site.id, event.id, uuid, name, fetched)

        return this[id]!! // TODO: cleanup
    }

    fun add(site: Site, event: Event, uuid: Uuid, name: String, fetched: Instant): Course {
        this[site, uuid]?.let { update(it.id, name, fetched); return it }

        return insert(site, event, uuid, name, fetched)
    }


    fun clearTutors(course: Course) = update("DELETE FROM tutor_courses WHERE course = ?", course.id)
    fun addTutor(user: User, course: Course) {
        insert("INSERT INTO tutor_courses(user, course) VALUES (?,?)", user.id, course.id)
    }

    fun clearEnrolled(course: Course) = update("DELETE FROM course_enrolled WHERE course = ?", course.id)
    fun addEnrolled(user: User, course: Course) {
        insert("INSERT INTO course_enrolled(user, course) VALUES (?,?)", user.id, course.id)
    }

    fun isTutor(): Boolean {
        val course = SqlBuilder.select("1").from(UserTable)
            .innerJoin(TutorCourses, TutorCourses.user eq UserTable.id)
            .where((UserTable.uuid eq AccountTable.uuid) and (UserTable.site eq AccountTable.site))

        val appointments = SqlBuilder.select("1").from(UserTable)
            .innerJoin(TutorAppointments, TutorAppointments.user eq UserTable.id)
            .where((UserTable.uuid eq AccountTable.uuid) and (UserTable.site eq AccountTable.site))

        val query = SqlBuilder.select("1").from(AccountTable)
            .where(SqlFilter.exists(course) or SqlFilter.exists(appointments))
            .limit(1)

        return storage.query(query) { it.isNotEmpty() }
    }

    fun isNotTutor(): Boolean { // TODO: This just checks if any account is not a tutor in any course (This should check if any account is not a tutor in any course)
        val tutors = SqlBuilder.select(UserTable.id).from(UserTable)
            .innerJoin(AccountTable, (UserTable.uuid eq AccountTable.uuid) and (UserTable.site eq AccountTable.site))

        val courses = SqlBuilder.select(TutorCourses.course).from(TutorCourses)
            .where(SqlFilter.contains(TutorCourses.user, tutors))

        // TODO: check appointments?

        val query = SqlBuilder.select("1").from(CourseTable)
            .where(SqlFilter.contains(CourseTable.id, courses).not())
            .limit(1)

        return storage.query(query) { it.isNotEmpty() }
    }

    operator fun get(account: Account) = storage.courses.all(
        SqlBuilder.select(CourseTable)
            .innerJoin(AccountCourses, AccountCourses.course eq id)
            .where(AccountCourses.account eq account.id)
    )

    companion object : SelectableSqlTableSchema<Course> {
        override val table get() = "courses"

        val id = column(Course::id)
        val site = column(Course::site)
        val event = column(Course::event)
        val uuid = column(Course::uuid)
        val name = column(Course::name)
        val fetched = column(Course::fetched)
        val fetchedEnrolled = column(Course::fetchedEnrolled)

        override val columns = listOf(id, site, event, uuid, name, fetched, fetchedEnrolled)

        override fun map(cursor: Cursor) = Course(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getUUID(3), cursor.getString(4), cursor.getInstant(5), cursor.getInstantOrNull(6))
    }
}
