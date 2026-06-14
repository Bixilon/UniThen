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
import de.bixilon.kutil.functions.FunctionUtil.letIf
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getInstant
import de.bixilon.unithen.storage.sql.SqlUtil.getInstantOrNull
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.SqlUtil.getUUIDOrNull
import de.bixilon.unithen.storage.sql.util.SelectableSqlTableSchema
import de.bixilon.unithen.storage.sql.util.SqlBuilder
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.eq
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.gt
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.isNotNull
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.isNull
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.lt
import de.bixilon.unithen.storage.sql.util.SqlTableSchema.Companion.column
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.User
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AppointmentTable(
    storage: SqlStorage,
) : SqlTable<Appointment>(storage, AppointmentTable) {

    operator fun get(id: Key) = single(AppointmentTable.id eq id)
    operator fun get(course: Course, uuid: Uuid) = single(SqlFilter.and("course" to course.id, "uuid" to uuid))

    operator fun get(course: Course?) = all(SqlFilter.and("course" to course?.id))
    operator fun get(uuid: Uuid) = all(SqlFilter.and("uuid" to uuid))

    fun getInRange(from: Instant, to: Instant, canceled: Boolean? = null, member: Boolean? = null, tutor: Boolean? = null): List<Appointment> {
        val _canceled = canceled?.let { if (it) AppointmentTable.canceled.isNotNull() else AppointmentTable.canceled.isNull() }
        val _member = member?.let { SqlFilter.exists(SqlBuilder.select("1").from(AccountCourses).where((AppointmentTable.course eq AccountCourses.course).letIf(it) { not() })) }
        val _tutor = tutor?.let { // TODO: OR is tutor for appointment
            val exists =
                if (it) {
                    SqlBuilder.select("1").from(AccountCourses)
                        .innerJoin(TutorCourses, (TutorCourses.user eq AccountCourses.account) and (TutorCourses.course eq AccountCourses.course))
                        .where(AccountCourses.course eq course)
                } else {
                    SqlBuilder.select("1").from(AccountCourses)
                        .where(AccountCourses.course eq course)
                        .and(SqlFilter.exists(
                            SqlBuilder.select("1").from(TutorCourses)
                                .where((TutorCourses.user eq AccountCourses.account) and (TutorCourses.course eq AccountCourses.course)))
                            .not())
                }
            return@let SqlFilter.exists(exists)
        }

        val _time = ((end lt from) or (start gt to)).not()

        val filter = _time and _canceled and _tutor and _member

        return all(select().where(filter).order(start, SqlBuilder.Order.Order.DESC))
    }

    fun update(id: Key, start: Instant? = null, end: Instant? = null, canceled: Instant? = null, location: String? = null, fetchedAttendees: Instant? = null) = update(id, SqlFilter.comma("start" to start, "end" to end, "canceled" to canceled, "location" to location, "fetched_attendees" to fetchedAttendees))


    fun insert(course: Course, uuid: Uuid, start: Instant, end: Instant, canceled: Instant?, location: String): Appointment {
        val id = storage.insert("INSERT INTO $table(course, uuid, start, end, canceled, location) VALUES (?,?,?,?,?,?)", course.id, uuid, start, end, canceled, location)

        return this[id]!!
    }

    fun add(course: Course, uuid: Uuid, start: Instant, end: Instant, canceled: Instant?, location: String): Appointment {
        this[course, uuid]?.let { update(it.id, start, end, canceled, location); return it }

        return insert(course, uuid, start, end, canceled, location)
    }

    fun clearAttendees(appointment: Appointment) = update("DELETE FROM appointment_attendees WHERE appointment=?", appointment.id)
    fun addAttendee(user: User, appointment: Appointment, attempt: Uuid) {
        insert("INSERT OR REPLACE INTO appointment_attendees(user, appointment, attempt) VALUES (?,?,?)", user.id, appointment.id, attempt)
    }

    fun removeAttendee(user: User, appointment: Appointment) {
        insert("DELETE FROM appointment_attendees WHERE user=? AND appointment=?", user.id, appointment.id)
    }

    fun getAttemptId(appointment: Appointment, user: User): Uuid? {
        return storage.query("SELECT attempt FROM appointment_attendees WHERE appointment=? AND user=?", appointment.id, user.id) { if (it.moveToNext()) it.getUUIDOrNull(0) else null }
    }


    fun clearTutors(appointment: Appointment) = update("DELETE FROM tutor_appointments WHERE appointment = ?", appointment.id)
    fun addTutor(user: User, appointment: Appointment) {
        insert("INSERT INTO tutor_appointments(user, appointment) VALUES (?,?)", user.id, appointment.id)
    }

    companion object : SelectableSqlTableSchema<Appointment> {
        override val table get() = "appointments"

        val id = column(Appointment::id)
        val course = column(Appointment::course)
        val uuid = column(Appointment::uuid)
        val start = column(Appointment::start)
        val end = column(Appointment::end)
        val canceled = column(Appointment::canceled)
        val location = column(Appointment::location)
        val fetchedAttendees = column(Appointment::fetchedAttendees)

        override val columns = listOf(id, course, uuid, start, end, canceled, location, fetchedAttendees)

        override fun map(cursor: Cursor) = Appointment(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getInstant(3), cursor.getInstant(4), cursor.getInstantOrNull(5), cursor.getString(6), cursor.getInstantOrNull(7))
    }
}
