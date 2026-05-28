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
import de.bixilon.kutil.exception.Broken
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getEnum
import de.bixilon.unithen.storage.sql.SqlUtil.getInstantOrNull
import de.bixilon.unithen.storage.sql.SqlUtil.getUUIDOrNull
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.CheckInAttempt
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil.SYNC_BACKOFF
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant


class CheckInAttemptsTable(
    storage: SqlStorage,
) : SqlTable<CheckInAttempt>(storage, "appointment_checkins") {
    override val columns = listOf("user", "appointment", "uuid", "time", "message", "sync", "status")

    override fun map(cursor: Cursor) = CheckInAttempt(cursor.getInt(0), cursor.getInt(1), cursor.getUUIDOrNull(2), cursor.getInstantOrNull(3), cursor.getStringOrNull(4), cursor.getInstantOrNull(5), cursor.getEnum(6, CheckInAttempt.Status))

    operator fun get(appointment: Appointment, uuid: UUID) = single(SqlFilter.and("appointment" to appointment.id, "uuid" to uuid))
    operator fun get(appointment: Appointment, user: User) = single(SqlFilter.and("appointment" to appointment.id, "user" to user.id))
    operator fun get(appointment: Appointment) = all(SqlFilter.and("appointment" to appointment.id) + " ORDER BY status")

    @Deprecated("data", level = DeprecationLevel.ERROR)
    fun update(appointment: Appointment, user: User): Nothing = Broken()

    fun update(appointment: Appointment, user: User, uuid: UUID? = null, time: Instant? = null, message: String? = null, sync: Instant? = null, status: CheckInAttempt.Status? = null) {
        val filter = SqlFilter.comma("uuid" to uuid, "time" to time, "message" to message, "sync" to sync, "status" to status)

        update("UPDATE $table SET ${filter.where} WHERE appointment=? AND user=?", parameters = arrayOf(*filter.parameters.toTypedArray(), appointment.id, user.id))
    }


    fun add(appointment: Appointment, user: User, uuid: UUID, message: String?, sync: Instant, status: CheckInAttempt.Status) {
        this[appointment, user]?.let { update(appointment, user, uuid, null, message, sync, status); return }

        insert("INSERT INTO $table(appointment, user, uuid, message, sync, status) VALUES (?,?,?,?,?,?)", appointment.id, user.id, uuid, message, sync, status)
    }

    fun add(appointment: Appointment, user: User, time: Instant, sync: Instant?): CheckInAttempt {
        insert("INSERT INTO $table(appointment, user, status, time, sync) VALUES (?,?,?,?,?)", appointment.id, user.id, CheckInAttempt.Status.PENDING, time, sync)

        return this[appointment, user]!!
    }

    fun getPendingSyncCount(): Int {
        return storage.query("SELECT COUNT(*) FROM $table WHERE state=?", CheckInAttempt.Status.PENDING) { it.collectIntAggregation() }
    }

    fun takePendingSync(): CheckInAttempt? {
        val time = Clock.System.now()
        val last = time - SYNC_BACKOFF

        return storage.transaction {
            val entry = single("state=? AND sync<?", CheckInAttempt.Status.PENDING, last) ?: return@transaction null

            update("UPDATE $table SET sync=? WHERE appointment=? AND user=?", time, entry.appointment, entry.user)

            return@transaction entry
        }
    }


    // fun clear(appointment: Appointment) = update("DELETE FROM $table WHERE appointment = ?", appointment.id)
}
