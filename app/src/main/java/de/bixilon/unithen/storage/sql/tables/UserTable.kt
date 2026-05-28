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
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.storage.types.User
import java.util.*

class UserTable(
    storage: SqlStorage,
) : SqlTable<User>(storage, "users") {
    override val columns = listOf("id", "site", "uuid", "firstname", "lastname")

    override fun map(cursor: Cursor) = User(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getString(3), cursor.getString(4))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(site: Site, uuid: UUID) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))

    fun update(id: Key, firstname: String? = null, lastname: String? = null) = update(id, SqlFilter.comma("firstname" to firstname, "lastname" to lastname))

    fun insert(site: Site, uuid: UUID, firstname: String, lastname: String): User {
        val id = insert("INSERT INTO $table(site, uuid, firstname, lastname) VALUES (?,?,?,?)", site.id, uuid, firstname, lastname)

        return this[id]!!
    }

    fun add(site: Site, uuid: UUID, firstname: String, lastname: String): User {
        this[site, uuid]?.let { update(it.id, firstname, lastname); return it }

        return insert(site, uuid, firstname, lastname)
    }


    fun getTutors(course: Course): List<User> {
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table INNER JOIN tutor_courses ON tutor_courses.user = $table.id WHERE course = ?", course.id) { it.collectAll() }
    }

    fun getTutors(appointment: Appointment): List<User> {
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table INNER JOIN tutor_appointments ON tutor_appointments.user = $table.id WHERE appointment = ?", appointment.id) { it.collectAll() }
    }

    fun getEnrolled(course: Course): List<User> {
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table INNER JOIN course_enrolled ON course_enrolled.user = $table.id WHERE course = ?", course.id) { it.collectAll() }
    }

    fun getEnrolledNotCheckedIn(appointment: Appointment, course: Course): List<User> {
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table INNER JOIN course_enrolled ON course_enrolled.user = $table.id WHERE course = ? AND NOT EXISTS (SELECT 1 FROM attempts WHERE attempts.appointment = ? AND attempts.user = $table.id)", course.id, appointment.id) { it.collectAll() }
    }

    fun getEnrolledCount(course: Course): Int {
        return storage.query("SELECT COUNT(*) FROM course_enrolled WHERE course = ?", course.id) { it.collectIntAggregation() }
    }

    fun isEnrolled(course: Course, user: User): Boolean {
        return storage.query("SELECT 1 FROM course_enrolled WHERE course=? AND user=?", course.id, user.id) { it.count > 0 } // TODO: verify
    }
}
