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
import de.bixilon.kutil.functions.FunctionUtil.letIf
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getInstantOrNull
import de.bixilon.unithen.storage.sql.SqlUtil.getUUIDOrNull
import de.bixilon.unithen.storage.sql.tables.UserTable.Companion.ftsEscape
import de.bixilon.unithen.storage.sql.util.SelectableSqlTableSchema
import de.bixilon.unithen.storage.sql.util.SqlBuilder
import de.bixilon.unithen.storage.sql.util.SqlFilter
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.eq
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.lt
import de.bixilon.unithen.storage.sql.util.SqlTableSchema.Companion.column
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.CheckInQueue
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil.SYNC_BACKOFF_FORCE
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil.SYNC_BACKOFF_NORMAL
import de.bixilon.unithen.ui.main.checkin.scan.attendees.AttendeeSort
import de.bixilon.unithen.ui.main.checkin.scan.attendees.Order
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid


class CheckInQueueTable(
    storage: SqlStorage,
) : SqlTable<CheckInQueue>(storage, CheckInQueueTable) {

    operator fun get(appointment: Appointment, uuid: Uuid) = single(SqlFilter.and("appointment" to appointment.id, "uuid" to uuid))
    operator fun get(appointment: Appointment, user: User) = single(SqlFilter.and("appointment" to appointment.id, "user" to user.id))
    operator fun get(appointment: Appointment) = all(select().where(SqlFilter.and("appointment" to appointment.id)))

    @Deprecated("data", level = DeprecationLevel.ERROR)
    fun update(appointment: Appointment, user: User): Nothing = Broken()

    fun update(appointment: Appointment, user: User, time: Instant? = null, attempt: Uuid? = null, message: String? = null, sync: Instant? = null) {
        val filter = SqlFilter.comma("time" to time, "attempt" to attempt, "message" to message, "sync" to sync)

        update("UPDATE $table SET ${filter.sql} WHERE appointment=? AND user=?", parameters = arrayOf(*filter.parameters.toTypedArray(), appointment.id, user.id))
    }

    fun delete(appointment: Appointment, user: User) {
        update("DELETE FROM $table WHERE appointment=? AND user=?", appointment.id, user.id)
    }


    fun addPending(appointment: Appointment, user: User, sync: Instant) {
        // TODO: This only works in android 9+
        insert("INSERT INTO $table(appointment, user, sync) VALUES (?,?,?) ON CONFLICT(appointment, user) DO UPDATE SET sync=?", appointment.id, user.id, sync, sync)
    }

    fun addCheckout(appointment: Appointment, user: User, attempt: Uuid, sync: Instant) {
        // TODO: This only works in android 9+
        insert("INSERT INTO $table(appointment, user, attempt, sync) VALUES (?,?,?,?) ON CONFLICT(appointment, user) DO UPDATE SET attempt=?, sync=?", appointment.id, user.id, attempt, sync, attempt, sync)
    }

    operator fun get(appointment: Appointment, search: String, sort: AttendeeSort, order: Order): List<CheckInQueue> {
        val query = SqlBuilder.select(CheckInQueueTable)
            .innerJoin("users", "checkin_queue.user = users.id")
            .letIf(search.isNotBlank()) { innerJoin("users_fts", "users.id = users_fts.docid") }
            .where(CheckInQueueTable.appointment eq appointment.id)
            .letIf(search.isNotBlank()) { and(SqlFilter("users_fts.fullname MATCH ?", "*${ftsEscape(search)}*")) }
            .order(
                sort.field to order.sql,
                AttendeeSort.next(sort).field to order.sql,
            )

        return storage.query(query) { it.collectAll() }

    }


    fun getCount(appointment: Appointment): Int {
        return storage.query(
            SqlBuilder.select(SqlBuilder.Aggregations.Count) from this where (CheckInQueueTable.appointment eq appointment.id))
        { it.collectIntAggregation() }
    }


    fun take(appointment: Appointment? = null, force: Boolean = false): CheckInQueue? {
        val time = Clock.System.now()
        val last = time - if (force) SYNC_BACKOFF_FORCE else SYNC_BACKOFF_NORMAL

        val _appointment = appointment?.let { CheckInQueueTable.appointment eq appointment.id }

        return storage.transaction {
            // TODO: Only sync if appointment end is still ahead of us
            val entry = first(select().where((sync lt last) and _appointment).limit(1)) ?: return@transaction null

            update("UPDATE $table SET sync=? WHERE appointment=? AND user=?", time, entry.appointment, entry.user)

            return@transaction entry
        }
    }

    companion object : SelectableSqlTableSchema<CheckInQueue> {
        override val table get() = "checkin_queue"

        val user = column(CheckInQueue::user)
        val appointment = column(CheckInQueue::appointment)
        val time = column(CheckInQueue::time)
        val attempt = column(CheckInQueue::attempt)
        val message = column(CheckInQueue::message)
        val sync = column(CheckInQueue::sync)

        override val columns = listOf(user, appointment, time, attempt, message, sync)

        override fun map(cursor: Cursor) = CheckInQueue(cursor.getInt(0), cursor.getInt(1), cursor.getInstantOrNull(2), cursor.getUUIDOrNull(3), cursor.getStringOrNull(4), cursor.getInstantOrNull(5))
    }
}
