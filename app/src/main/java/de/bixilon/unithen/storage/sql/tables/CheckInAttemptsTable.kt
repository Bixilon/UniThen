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
import de.bixilon.unithen.storage.sql.tables.UserTable.Companion.applyIf
import de.bixilon.unithen.storage.sql.tables.UserTable.Companion.ftsEscape
import de.bixilon.unithen.storage.sql.util.SqlBuilder
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.eq
import de.bixilon.unithen.storage.sql.util.SqlSchema
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.CheckInAttempt
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil.SYNC_BACKOFF
import de.bixilon.unithen.ui.main.checkin.scan.attendees.AttendeeSort
import de.bixilon.unithen.ui.main.checkin.scan.attendees.Order
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant


class CheckInAttemptsTable(
    storage: SqlStorage,
) : SqlTable<CheckInAttempt>(storage, table) {
    override val columns get() = CheckInAttemptsTable.columns

    override fun map(cursor: Cursor) = CheckInAttemptsTable.map(cursor)

    operator fun get(appointment: Appointment, uuid: UUID) = single(SqlFilter.and("appointment" to appointment.id, "uuid" to uuid))
    operator fun get(appointment: Appointment, user: User) = single(SqlFilter.and("appointment" to appointment.id, "user" to user.id))
    operator fun get(appointment: Appointment) = all(SqlFilter.and("appointment" to appointment.id) + " ORDER BY status")

    @Deprecated("data", level = DeprecationLevel.ERROR)
    fun update(appointment: Appointment, user: User): Nothing = Broken()

    fun update(appointment: Appointment, user: User, uuid: UUID? = null, time: Instant? = null, message: String? = null, sync: Instant? = null, status: CheckInAttempt.Status? = null) {
        val filter = SqlFilter.comma("uuid" to uuid, "time" to time, "message" to message, "sync" to sync, "status" to status)

        update("UPDATE $table SET ${filter.sql} WHERE appointment=? AND user=?", parameters = arrayOf(*filter.parameters.toTypedArray(), appointment.id, user.id))
    }

    fun delete(appointment: Appointment, user: User) {
        update("DELETE FROM $table WHERE appointment=? AND user=?", appointment.id, user.id)
    }


    fun add(appointment: Appointment, user: User, uuid: UUID, message: String?, sync: Instant, status: CheckInAttempt.Status) {
        this[appointment, user]?.let { update(appointment, user, uuid, null, message, sync, status); return }

        insert("INSERT INTO $table(appointment, user, uuid, message, sync, status) VALUES (?,?,?,?,?,?)", appointment.id, user.id, uuid, message, sync, status)
    }

    fun add(appointment: Appointment, user: User, time: Instant, sync: Instant?): CheckInAttempt {
        insert("INSERT INTO $table(appointment, user, status, time, sync) VALUES (?,?,?,?,?)", appointment.id, user.id, CheckInAttempt.Status.PENDING, time, sync)

        return this[appointment, user]!!
    }

    operator fun get(appointment: Appointment, search: String, sort: AttendeeSort, order: Order): List<CheckInAttempt> {
        val query = SqlBuilder.select(CheckInAttemptsTable)
            .innerJoin("users", "appointment_checkins.user = users.id")
            .applyIf(search.isNotBlank()) { innerJoin("users_fts", "users.id = users_fts.docid") }
            .where(CheckInAttempt::appointment eq appointment.id)
            .applyIf(search.isNotBlank()) { and(SqlFilter("users_fts.fullname MATCH ?", "*${ftsEscape(search)}*")) }
            .order(
                sort.field to order.sql,
                (if (sort == AttendeeSort.FIRSTNAME) AttendeeSort.LASTNAME else AttendeeSort.FIRSTNAME).field to order.sql, // TODO: Enum::next (kutil 1.32)
            )

        return storage.query(query) { it.collectAll() }

    }


    fun getPendingSyncCount(appointment: Appointment? = null): Int {
        return storage.query(
            SqlBuilder.select(SqlBuilder.Aggregations.Count) from this where (CheckInAttempt::status eq CheckInAttempt.Status.PENDING and appointment?.let { CheckInAttempt::appointment eq appointment.id }))
        { it.collectIntAggregation() }
    }


    fun takePendingSync(appointment: Appointment? = null): CheckInAttempt? {
        val time = Clock.System.now()
        val last = time - SYNC_BACKOFF

        val _appointment = appointment?.let { CheckInAttempt::appointment eq appointment.id }

        return storage.transaction {
            val entry = first(SqlFilter("status=? AND sync<?", CheckInAttempt.Status.PENDING, last) and _appointment) ?: return@transaction null

            update("UPDATE $table SET sync=? WHERE appointment=? AND user=?", time, entry.appointment, entry.user)

            return@transaction entry
        }
    }

    companion object : SqlSchema<CheckInAttempt> {
        override val table get() = "appointment_checkins"
        override val columns = listOf("user", "appointment", "uuid", "time", "message", "sync", "status")

        override fun map(cursor: Cursor) = CheckInAttempt(cursor.getInt(0), cursor.getInt(1), cursor.getUUIDOrNull(2), cursor.getInstantOrNull(3), cursor.getStringOrNull(4), cursor.getInstantOrNull(5), cursor.getEnum(6, CheckInAttempt.Status))
    }
}
