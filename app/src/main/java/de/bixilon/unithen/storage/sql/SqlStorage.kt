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

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteStatement
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.database.sqlite.transaction
import de.bixilon.unithen.storage.DefaultStorage
import de.bixilon.unithen.storage.sql.SqlUtil.db
import de.bixilon.unithen.storage.sql.tables.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.Closeable
import org.intellij.lang.annotations.Language
import java.util.*
import kotlin.time.Instant

class SqlStorage(context: Context) : Closeable {
    val helper = SqlHelper(context)


    private val notify = mutableIntStateOf(0) // TODO: Kind of a hack

    val sites = SiteTable(this)
    val events = EventTable(this)
    val users = UserTable(this)
    val accounts = AccountTable(this)
    val courses = CourseTable(this)
    val appointments = AppointmentTable(this)

    init {
        if (sites.count == 0) {
            // TODO: sync ui with this?
            CoroutineScope(Dispatchers.IO).launch { DefaultStorage.SITES.forEach { sites.add(it) } }
        }
    }


    fun notifyState() {
        TRANSACTIONS.get()?.let { it += notify; return }

        CoroutineScope(Dispatchers.Default).launch {
            notify.intValue++
        }
    }

    private fun SQLiteStatement.bind(vararg parameters: Any?) {
        for ((index, parameter) in parameters.withIndex()) {
            val actual = index + 1
            when (parameter) {
                null -> bindNull(actual)
                is Int -> bindLong(actual, parameter.toLong())
                is Long -> bindLong(actual, parameter)
                is String -> bindString(actual, parameter)
                is Instant -> bindLong(actual, parameter.epochSeconds)
                is UUID -> bindString(actual, parameter.toString())
                is ByteArray -> bindBlob(actual, parameter)
                else -> throw IllegalArgumentException("Unknown parameter type: $parameter")
            }
        }
    }

    fun <T> query(@Language("SQL") sql: String, vararg parameters: Any?, runnable: (Cursor) -> T): T {
        return helper.readableDatabase.rawQuery(sql, parameters.map { it.db() }.toTypedArray()).use { runnable.invoke(it) }
    }

    fun insert(@Language("SQL") sql: String, vararg parameters: Any?): Int {
        val statement = helper.writableDatabase.compileStatement(sql)

        statement.bind(*parameters)

        return statement.use { it.executeInsert().toInt() }
    }

    fun update(@Language("SQL") sql: String, vararg parameters: Any?): Int {
        val statement = helper.writableDatabase.compileStatement(sql)

        statement.bind(*parameters)

        return statement.use { it.executeUpdateDelete() }
    }

    inline fun <T> transaction(crossinline block: (SqlStorage) -> T): T {
        if (TRANSACTIONS.get() != null) throw IllegalStateException("Nested transactions are forbidden!")
        val set: MutableSet<MutableIntState> = mutableSetOf()
        try {
            TRANSACTIONS.set(set)
            return helper.writableDatabase.transaction { block.invoke(this@SqlStorage) }
        } finally {
            CoroutineScope(Dispatchers.Main).launch { set.forEach { it.intValue++ } }
            TRANSACTIONS.remove()
        }
    }

    override fun close() {
        helper.close()
    }

    fun <T> stateOf(block: SqlStorage.() -> T): State<T> {
        return derivedStateOf { notify.intValue; block.invoke(this) }
    }


    companion object {
        val TRANSACTIONS = ThreadLocal<MutableSet<MutableIntState>>()
    }
}
