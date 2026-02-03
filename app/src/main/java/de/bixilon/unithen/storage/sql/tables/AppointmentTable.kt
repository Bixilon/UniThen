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
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getInstant
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.util.*
import kotlin.time.Instant

class AppointmentTable(
    storage: SqlStorage,
) : SqlTable<Appointment>(storage, "appointments") {
    override val columns = listOf("id", "course", "uuid", "start", "end", "location")


    override fun map(cursor: Cursor) = Appointment(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getInstant(3), cursor.getInstant(4), cursor.getString(5))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(course: Course, uuid: UUID) = single(SqlFilter.and("course" to course.id, "uuid" to uuid))

    operator fun get(course: Course?) = all(SqlFilter.and("course" to course?.id))

    fun getInRange(from: Instant, to: Instant): List<Appointment> {
        val filter = SqlFilter("NOT (end < ? OR start > ?) ORDER BY start DESC", listOf(from, to))

        return all(filter)
    }

    fun update(id: Key, start: Instant? = null, end: Instant? = null, location: String? = null) = update(id, SqlFilter.comma("start" to start, "end" to end, "location" to location))


    fun insert(course: Course, uuid: UUID, start: Instant, end: Instant, location: String): Appointment {
        val id = storage.insert("INSERT INTO $table(course, uuid, start, end, location) VALUES (?,?,?,?,?)", course.id, uuid, start, end, location)

        return this[id]!!
    }

    fun add(course: Course, uuid: UUID, start: Instant, end: Instant, location: String): Appointment {
        this[course, uuid]?.let { update(it.id, start, end, location); return it }

        return insert(course, uuid, start, end, location)
    }
}
