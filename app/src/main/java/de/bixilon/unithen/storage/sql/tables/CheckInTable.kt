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
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getEnum
import de.bixilon.unithen.storage.sql.SqlUtil.getInstant
import de.bixilon.unithen.storage.sql.SqlUtil.getInstantOrNull
import de.bixilon.unithen.storage.sql.SqlUtil.getUUIDOrNull
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.CheckIn
import de.bixilon.unithen.storage.types.User
import java.util.*
import kotlin.time.Instant


class CheckInTable(
    storage: SqlStorage,
) : SqlTable<CheckIn>(storage, "appointment_checkins") {
    override val columns = listOf("user", "appointment", "uuid", "time", "message", "sync", "status")

    override fun map(cursor: Cursor) = CheckIn(cursor.getInt(0), cursor.getInt(1), cursor.getUUIDOrNull(2), cursor.getInstant(3), cursor.getStringOrNull(4), cursor.getInstantOrNull(5), cursor.getEnum(6, CheckIn.Status))

    operator fun get(appointment: Appointment, uuid: UUID) = single(SqlFilter.and("appointment" to appointment.id, "uuid" to uuid))
    operator fun get(appointment: Appointment, user: User) = single(SqlFilter.and("appointment" to appointment.id, "user" to user.id))
    operator fun get(appointment: Appointment) = all(SqlFilter.and("appointment" to appointment.id))


    fun getNotOk(appointment: Appointment) = all(SqlFilter.and("appointment" to appointment.id) and SqlFilter.or("status" to CheckIn.Status.PENDING, "status" to CheckIn.Status.FAILED))

    fun add(appointment: Appointment, user: User, uuid: UUID, message: String?, sync: Instant, status: CheckIn.Status) {
        insert("INSERT INTO $table(appointment, user, uuid, message, sync, status) VALUES (?,?,?,?,?,?)", appointment.id, user.id, uuid, message, sync, status)
    }

    fun add(appointment: Appointment, user: User): CheckIn {
        insert("INSERT INTO $table(appointment, user, status) VALUES (?,?,?)", appointment.id, user.id, CheckIn.Status.PENDING)

        return this[appointment, user]!!
    }


    // fun clear(appointment: Appointment) = update("DELETE FROM $table WHERE appointment = ?", appointment.id)
}
