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

import co.touchlab.sqliter.*
import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import kotlin.time.Instant
import kotlin.uuid.Uuid

class NativeSQLiteHelper(val name: String?) : SQLiteHelper {
    private val lock = Lock.lock()
    val driver by lazy { createDatabaseManager(DatabaseConfiguration(name, SqlStorage.VERSION, create = this::create, upgrade = this::upgrade, inMemory = name == null)).createMultiThreadedConnection() }


    private fun DatabaseConnection.executeBatch(path: String) {
        val statements = SqlUtil.split(SqlUtil.load(path))
        statements.forEach { this.rawExecSql(it) }
    }

    private fun create(database: DatabaseConnection) {
        database.executeBatch("schema")
    }

    private fun upgrade(database: DatabaseConnection, start: Int, end: Int) = database.withTransaction {
        for (version in (start + 1)..end) {
            try {
                database.executeBatch("migrations/${version}")
            } catch (error: Throwable) {
                throw Exception("Error during database migration $version: ${error.message}", error)
            }
        }
    }

    override fun load() {
        driver
    }

    private fun Statement.bind(vararg parameters: Any?) {
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

    private fun createStatement(sql: String, vararg parameters: Any?): Statement {
        val statement = driver.createStatement(sql)
        statement.bind(*parameters)

        return statement
    }


    override fun query(sql: String, vararg parameters: Any?): SQLiteHelper.Cursor {
        val statement = createStatement(sql, *parameters)

        return NativeCursor(statement, statement.query())
    }

    override fun execute(sql: String, vararg parameters: Any?): Int {
        val statement = createStatement(sql, *parameters)

        return statement.executeUpdateDelete()
    }

    override fun insert(sql: String, vararg parameters: Any?): Int {
        val statement = createStatement(sql, *parameters)

        return statement.executeInsert().toInt() // TODO: This returns the rowid, not the auto increment id
    }

    override fun <T> transaction(block: () -> T): T {
        return lock.locked { driver.withTransaction { block.invoke() } }
    }

    override fun close() {
        driver.close()
    }

    class NativeCursor(val statement: Statement, val cursor: Cursor) : SQLiteHelper.Cursor {
        override fun getBlob(index: Int) = cursor.getBytes(index)
        override fun getBlobOrNull(index: Int) = cursor.getBytes(index)

        override fun getString(index: Int) = cursor.getString(index)
        override fun getStringOrNull(index: Int) = cursor.getString(index)

        override fun getInt(index: Int) = cursor.getLong(index).toInt()
        override fun getLong(index: Int) = cursor.getLong(index)

        override fun isNull(index: Int) = cursor.isNull(index)

        override fun moveToNext() = cursor.next()

        override fun close() = statement.finalizeStatement()

        override fun isEmpty() = !cursor.next()
    }
}
