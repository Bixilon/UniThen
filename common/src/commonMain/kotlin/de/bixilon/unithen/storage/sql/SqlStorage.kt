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

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import de.bixilon.unithen.storage.sql.tables.*
import de.bixilon.unithen.storage.sql.util.SqlBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import java.io.Closeable

class SqlStorage(val helper: SQLiteHelper) : Closeable {

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
        TRANSACTIONS.get()?.let { it += notify; return }

        scope.launch { notify.intValue++ }
    }


    fun <T> query(statement: SqlBuilder.Executable, runnable: (SQLiteHelper.Cursor) -> T) = query(statement.toSql(), runnable)
    fun <T> query(statement: SqlBuilder.SqlStatement, runnable: (SQLiteHelper.Cursor) -> T) = query(statement.sql, parameters = statement.parameters.toTypedArray(), runnable)

    fun <T> query(@Language("SQL") sql: String, vararg parameters: Any?, runnable: (SQLiteHelper.Cursor) -> T): T {
        return helper.query(sql, *parameters).use { runnable.invoke(it) }
    }

    fun insert(@Language("SQL") sql: String, vararg parameters: Any?): Long {
        return helper.insert(sql, *parameters).apply { notifyState() }
    }

    fun update(@Language("SQL") sql: String, vararg parameters: Any?): Int {
        return helper.execute(sql, *parameters).apply { notifyState() }
    }

    inline fun <T> transaction(crossinline block: (SqlStorage) -> T): T {
        if (TRANSACTIONS.get() != null) throw IllegalStateException("Nested transactions are forbidden!")
        val set: MutableSet<MutableIntState> = mutableSetOf()
        try {
            TRANSACTIONS.set(set)
            return helper.transaction { block.invoke(this@SqlStorage) }
        } finally {
            scope.launch { set.forEach { it.intValue++ } }
            TRANSACTIONS.remove()
        }
    }

    override fun close() {
        helper.close()
    }

    fun cleanup() {
        helper.executeBatch("cleanup")
        insert("VACUUM")
    }

    fun clearCache() {
        helper.executeBatch("clear_cache")
        insert("VACUUM")
    }

    companion object {
        val TRANSACTIONS = ThreadLocal<MutableSet<MutableIntState>>()
    }
}
