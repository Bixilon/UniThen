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

package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.SqlUtil.db
import de.bixilon.unithen.storage.sql.SqlUtil.getLocalDate
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.time.LocalDateTime
import java.util.*

class AppointmentTable(
    storage: SqlStorage,
) : SqlTable<Appointment>(storage, "appointments") {
    override val columns = listOf("id", "course", "uuid", "start", "end")


    override fun map(cursor: Cursor) = Appointment(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getLocalDate(3), cursor.getLocalDate(4))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(course: Course, uuid: UUID) = single(SqlFilter.and("course" to course.id, "uuid" to uuid))

    operator fun get(course: Course?) = all(SqlFilter.and("course" to course?.id))

    fun getInRange(from: LocalDateTime, to: LocalDateTime): List<Appointment> {
        val filter = SqlFilter("NOT (end < ? OR start > ?) ORDER BY start DESC", listOf(from.db(), to.db()))

        return all(filter)
    }

    fun update(id: Key, start: LocalDateTime? = null, end: LocalDateTime? = null) = update(id, SqlFilter.comma("start" to start?.db(), "end" to end?.db()))


    fun insert(course: Course, uuid: UUID, start: LocalDateTime, end: LocalDateTime) {
        storage.execute("INSERT INTO $table(course, uuid, start, end) VALUES (?,?,?,?)", course.id, uuid, start.db(), end.db())
    }

    fun add(course: Course, uuid: UUID, start: LocalDateTime, end: LocalDateTime) {
        this[course, uuid]?.let { return update(it.id, start, end) }

        insert(course, uuid, start, end)
    }
}
