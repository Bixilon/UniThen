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

import androidx.compose.runtime.mutableIntStateOf
import de.bixilon.unithen.storage.sql.tables.*
import de.bixilon.unithen.storage.sql.util.SqlBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

expect var transaction: SQLiteHelper.UpdateConnection?

class SqlStorage(val helper: SQLiteHelper) : AutoCloseable {
    val scope = CoroutineScope(Dispatchers.Main)
    val notify = mutableIntStateOf(0) // TODO: Kind of a hack

    val sites = SiteTable(this)
    val events = EventTable(this)
    val users = UserTable(this)
    val accounts = AccountTable(this)
    val courses = CourseTable(this)
    val appointments = AppointmentTable(this)
    val checkInQueue = CheckInQueueTable(this)


    fun notifyState() {
        if (transaction != null) return

        scope.launch { notify.intValue++ }
    }


    fun <T> query(statement: SqlBuilder.Executable, runnable: (SQLiteHelper.Cursor) -> T) = query(statement.toSql(), runnable)
    fun <T> query(statement: SqlBuilder.SqlStatement, runnable: (SQLiteHelper.Cursor) -> T) = query(statement.sql, parameters = statement.parameters.toTypedArray(), runnable)

    fun <T> query(sql: String, vararg parameters: Any?, runnable: (SQLiteHelper.Cursor) -> T): T {
        val transaction = transaction
        if (transaction != null) {
            return transaction.query(sql, *parameters).use { runnable.invoke(it) }
        }
        return helper.query().use { it.query(sql, *parameters).use { runnable.invoke(it) } }
    }

    fun insert(sql: String, vararg parameters: Any?): Int {
        val transaction = transaction
        if (transaction != null) {
            return transaction.insert(sql, *parameters).apply { notifyState() }
        }
        return helper.update().use { it.insert(sql, *parameters).apply { notifyState() } }
    }

    fun update(sql: String, vararg parameters: Any?): Int {
        val transaction = transaction
        if (transaction != null) {
            return transaction.execute(sql, *parameters).apply { notifyState() }
        }
        return helper.update().use { it.execute(sql, *parameters).apply { notifyState() } }
    }

    inline fun <T> transaction(crossinline block: (SqlStorage) -> T): T {
        val connection = helper.update()
        try {
            transaction = connection
            return connection.transaction { block.invoke(this@SqlStorage) }
        } finally {
            transaction = null
            notifyState()
        }
    }

    override fun close() {
        helper.close()
    }

    fun cleanup(): Unit = helper.update().use {
        it.transaction { it.executeBatch("cleanup") }
        it.execute("VACUUM")
    }

    fun clearCache(): Unit = helper.update().use {
        it.transaction { it.executeBatch("clear_cache") }
        it.execute("VACUUM")
    }

    companion object {
        const val VERSION = 10
    }
}
