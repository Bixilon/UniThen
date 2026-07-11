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
import java.io.IOException
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AndroidSqlHelper(context: Context) : SQLiteOpenHelper(context, NAME, null, VERSION), SQLiteHelper {

    private fun SQLiteDatabase.executeBatch(path: String) {
        val statements = SqlUtil.split(path)
        transaction { statements.forEach { execSQL(it) } }
    }

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

    override fun executeBatch(path: String) {
        writableDatabase.executeBatch(path)
    }

    override fun <T> transaction(block: () -> T): T {
        return writableDatabase.transaction { block.invoke() }
    }

    private fun createStatement(readonly: Boolean, sql: String, vararg parameters: Any?): SQLiteStatement {
        val database = if (readonly) readableDatabase else writableDatabase

        val statement = database.compileStatement(sql)
        statement.bind(*(parameters.map { it.db() }.toTypedArray()))

        return statement
    }

    override fun execute(sql: String, vararg parameters: Any?): Int {
        val statement = createStatement(false, sql, *parameters)

        return statement.use { it.executeUpdateDelete() }
    }

    override fun insert(sql: String, vararg parameters: Any?): Long {
        val statement = createStatement(false, sql, *parameters)

        return statement.use { it.executeInsert() }
    }

    override fun query(sql: String, vararg parameters: Any?): SQLiteHelper.Cursor {
        return AndroidCursor(readableDatabase.rawQuery(sql, parameters.map { it.db() }.toTypedArray()))
    }


    fun Any?.db(): String? = when (this) {
        null -> null
        is Int -> this.toString()
        is Long -> this.toString()
        is String -> this
        is Uuid -> this.toString()
        is Instant -> epochSeconds.toString()
        is Enum<*> -> name
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
                else -> throw IllegalArgumentException("Unknown parameter type: $parameter")
            }
        }
    }

    override fun close() {
        super.close()
    }

    override fun load() {
        writableDatabase
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
        override fun moveToPrevious() = cursor.moveToPrevious()

        override fun close() = cursor.close()
    }

    companion object {
        const val NAME = "uninow"
        const val VERSION = 9
    }
}
