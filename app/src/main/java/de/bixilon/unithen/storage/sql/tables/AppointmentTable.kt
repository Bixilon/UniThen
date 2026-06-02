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
import de.bixilon.unithen.storage.sql.SqlUtil.getUUIDOrNull
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.isNotNull
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.isNull
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.User
import java.util.*
import kotlin.time.Instant

class AppointmentTable(
    storage: SqlStorage,
) : SqlTable<Appointment>(storage, "appointments") {
    override val columns = listOf("id", "course", "uuid", "start", "end", "canceled", "location", "attendees_fetched")


    override fun map(cursor: Cursor) = Appointment(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getInstant(3), cursor.getInstant(4), cursor.getInstantOrNull(5), cursor.getString(6), cursor.getInstantOrNull(7))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(course: Course, uuid: UUID) = single(SqlFilter.and("course" to course.id, "uuid" to uuid))

    operator fun get(course: Course?) = all(SqlFilter.and("course" to course?.id))

    fun getInRange(from: Instant, to: Instant, canceled: Boolean? = null, member: Boolean? = null, tutor: Boolean? = null): List<Appointment> {
        val _canceled = canceled?.let { if (it) Appointment::canceled.isNotNull() else Appointment::canceled.isNull() }
        val _member = member?.let { val not = if (it) "" else "NOT"; SqlFilter("$not EXISTS (SELECT 1 FROM account_courses WHERE $table.course = account_courses.course)") }
        val _tutor = tutor?.let {
            if(it) {
                SqlFilter("EXISTS (SELECT 1 FROM account_courses JOIN tutor_courses ON tutor_courses.user = account_courses.account AND tutor_courses.course = account_courses.course WHERE account_courses.course = $table.course)")
            }else {
                SqlFilter("EXISTS (SELECT 1 FROM account_courses WHERE account_courses.course = appointments.course AND NOT EXISTS (SELECT 1 FROM tutor_courses WHERE tutor_courses.user = account_courses.account AND tutor_courses.course = account_courses.course))")
            }
        }

        val filter = SqlFilter("NOT (end < ? OR start > ?)", listOf(from, to)) and _canceled and _tutor and _member

        return all(filter + "ORDER BY start DESC")
    }

    fun update(id: Key, start: Instant? = null, end: Instant? = null, canceled: Instant? = null, location: String? = null, attendeesFetched: Instant? = null) = update(id, SqlFilter.comma("start" to start, "end" to end, "canceled" to canceled, "location" to location, "attendees_fetched" to attendeesFetched))


    fun insert(course: Course, uuid: UUID, start: Instant, end: Instant, canceled: Instant?, location: String): Appointment {
        val id = storage.insert("INSERT INTO $table(course, uuid, start, end, canceled, location) VALUES (?,?,?,?,?,?)", course.id, uuid, start, end, canceled, location)

        return this[id]!!
    }

    fun add(course: Course, uuid: UUID, start: Instant, end: Instant, canceled: Instant?, location: String): Appointment {
        this[course, uuid]?.let { update(it.id, start, end, canceled, location); return it }

        return insert(course, uuid, start, end, canceled, location)
    }

    fun clearAttendees(appointment: Appointment) = update("DELETE FROM appointment_attendees WHERE appointment=?", appointment.id)
    fun addAttendee(user: User, appointment: Appointment, attempt: UUID) {
        insert("INSERT INTO appointment_attendees(user, appointment, attempt) VALUES (?,?,?) ON CONFLICT(user, appointment) DO NOTHING", user.id, appointment.id, attempt)
    }
    fun removeAttendee(user: User, appointment: Appointment) {
        insert("DELETE FROM appointment_attendees WHERE user=? AND appointment=?", user.id, appointment.id)
    }

    fun getAttemptId(appointment: Appointment, user: User): UUID? {
        return storage.query("SELECT attempt FROM $table WHERE appointment=? AND user=?", appointment.id, user.id) { it.getUUIDOrNull(0) }
    }



    fun clearTutors(appointment: Appointment) = update("DELETE FROM tutor_appointments WHERE appointment = ?", appointment.id)
    fun addTutor(user: User, appointment: Appointment) {
        insert("INSERT INTO tutor_appointments(user, appointment) VALUES (?,?) ON CONFLICT(user, appointment) DO NOTHING", user.id, appointment.id)
    }
}
