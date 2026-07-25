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
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import androidx.core.database.getBlobOrNull
import androidx.core.database.sqlite.transaction
import de.bixilon.kutil.primitive.IntUtil.toInt
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import java.io.IOException
import kotlin.time.Instant
import kotlin.uuid.Uuid

private fun SQLiteDatabase.executeBatch(path: String) {
    val statements = SqlUtil.split(SqlUtil.load(path))
    statements.forEach { execSQL(it) }
}

private fun Any?.db(): String? = when (this) {
    null -> null
    is Int -> this.toString()
    is Long -> this.toString()
    is String -> this
    is Uuid -> this.toString()
    is Instant -> epochSeconds.toString()
    is Enum<*> -> name
    is Boolean -> this.toString()
    else -> throw IllegalArgumentException("Unknown parameter type: $this")
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
            is Uuid -> bindString(actual, parameter.toString())
            is ByteArray -> bindBlob(actual, parameter)
            is Enum<*> -> bindString(actual, parameter.name)
            is Boolean -> bindLong(actual, parameter.toInt().toLong())
            else -> throw IllegalArgumentException("Unknown parameter type: $parameter")
        }
    }
}

class AndroidSqlHelper(context: Context) : SQLiteOpenHelper(context, NAME, null, SqlStorage.VERSION), SQLiteHelper {
    private var lock = Mutex(false)

    override fun onCreate(database: SQLiteDatabase) {
        database.executeBatch("schema")
    }

    override fun onUpgrade(database: SQLiteDatabase, start: Int, end: Int) = database.transaction {
        for (version in (start + 1)..end) {
            try {
                database.executeBatch("migrations/${version}")
            } catch (error: Throwable) {
                throw IOException("Error during database migration $version: ${error.message}", error)
            }
        }
    }

    override fun query(): SQLiteHelper.QueryConnection {
        runBlocking { lock.lock() }
        return AndroidQueryConnection(readableDatabase)
    }

    override fun update(): SQLiteHelper.UpdateConnection {
        runBlocking { lock.lock() }
        return AndroidUpdateConnection(writableDatabase)
    }

    override fun close() {
        super.close()
    }

    override suspend fun load() {
        writableDatabase
    }

    private open inner class AndroidQueryConnection(val database: SQLiteDatabase) : SQLiteHelper.QueryConnection {

        override fun query(sql: String, vararg parameters: Any?): SQLiteHelper.Cursor {
            // TODO: That sucks, we must convert all parameters to a string...
            return AndroidCursor(database.rawQuery(sql, parameters.map { it.db() }.toTypedArray()))
        }

        override fun close() {
            lock.unlock()
        }

    }

    private inner class AndroidUpdateConnection(database: SQLiteDatabase) : AndroidQueryConnection(database), SQLiteHelper.UpdateConnection {
        private var transaction = false

        override fun executeBatch(path: String) {
            database.executeBatch(path)
        }

        @Synchronized
        override fun <T> transaction(block: () -> T): T {
            if (transaction) throw IllegalStateException("Nested transactions are unsupported!")
            this.transaction = true
            try {
                return database.transaction { block.invoke() }
            } finally {
                transaction = false
            }
        }

        private fun createStatement(sql: String, vararg parameters: Any?): SQLiteStatement {
            val statement = database.compileStatement(sql)
            statement.bind(*parameters)

            return statement
        }

        override fun execute(sql: String, vararg parameters: Any?): Int {
            val statement = createStatement(sql, *parameters)

            return statement.use { it.executeUpdateDelete() }
        }

        override fun insert(sql: String, vararg parameters: Any?): Int {
            val statement = createStatement(sql, *parameters)

            return statement.use { it.executeInsert().toInt() }  // TODO: That is bad, it is returning the row id, not the id
        }
    }

    class AndroidCursor(val cursor: Cursor) : SQLiteHelper.Cursor {
        override fun getBlob(index: Int) = cursor.getBlob(index)
        override fun getBlobOrNull(index: Int) = cursor.getBlobOrNull(index)

        override fun getString(index: Int) = cursor.getString(index)
        override fun getStringOrNull(index: Int) = cursor.getString(index)

        override fun getInt(index: Int) = cursor.getInt(index)
        override fun getLong(index: Int) = cursor.getLong(index)

        override fun isNull(index: Int) = cursor.isNull(index)

        override fun moveToNext() = cursor.moveToNext()

        override fun close() = cursor.close()
    }

    companion object {
        const val NAME = "uninow"
    }
}
