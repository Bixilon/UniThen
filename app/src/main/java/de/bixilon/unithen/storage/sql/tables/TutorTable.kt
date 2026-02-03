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
import de.bixilon.unithen.storage.*
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.util.*

class TutorTable(
    storage: SqlStorage,
) : SqlTable<Tutor>(storage, "tutors") {

    override val columns = listOf("id", "site", "uuid", "first_name", "last_name")

    override fun map(cursor: Cursor) = Tutor(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getString(3), cursor.getString(4))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(site: Site, uuid: UUID) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))

    fun update(id: Key, firstName: String? = null, lastName: String? = null) = update(id, SqlFilter.comma("first_name" to firstName, "last_name" to lastName))

    fun insert(site: Site, uuid: UUID, firstName: String, lastName: String): Tutor {
        val id = insert("INSERT INTO $table(site, uuid, first_name, last_name) VALUES (?,?,?,?)", site.id, uuid, firstName, lastName)

        return this[id]!!
    }

    fun add(site: Site, uuid: UUID, firstName: String, lastName: String): Tutor {
        this[site, uuid]?.let { update(it.id, firstName, lastName); return it }

        return insert(site, uuid, firstName, lastName)
    }


    operator fun get(course: Course): List<Tutor> {
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table INNER JOIN tutor_courses ON tutor_courses.tutor = $table.id WHERE course = ?", course.id) { it.collectAll() }
    }

    operator fun get(appointment: Appointment): List<Tutor> {
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table INNER JOIN tutor_appointments ON tutor_appointments.tutor = $table.id WHERE appointment = ?", appointment.id) { it.collectAll() }
    }

    fun addTo(tutor: Tutor, course: Course) {
        insert("INSERT INTO tutor_courses(tutor, course) VALUES (?,?) ON CONFLICT(tutor, course) DO NOTHING", tutor.id, course.id)
        storage.courses.notify.intValue++
    }

    fun addTo(tutor: Tutor, appointment: Appointment) {
        insert("INSERT INTO tutor_appointments(tutor, appointment) VALUES (?,?) ON CONFLICT(tutor, appointment) DO NOTHING", tutor.id, appointment.id)
        storage.appointments.notify.intValue++
    }
}
