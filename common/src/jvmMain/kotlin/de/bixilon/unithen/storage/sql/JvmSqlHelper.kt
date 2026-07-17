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

import java.io.File
import java.io.IOException
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import kotlin.time.Instant
import kotlin.uuid.Uuid

class JvmSqlHelper(file: File?) : SQLiteHelper {
    private val connection by lazy { DriverManager.getConnection(if (file == null) "jdbc:sqlite::memory:" else "jdbc:sqlite:$file") }

    init {
        Class.forName("org.sqlite.JDBC", true, JvmSqlHelper::class.java.classLoader)
    }

    private val version get() = createStatement("PRAGMA user_version;").executeQuery().use { it.next(); it.getInt(1) }

    override fun load() {
        val version = version
        if (version == SqlStorage.VERSION) return
        if (version > SqlStorage.VERSION) {
            throw IllegalStateException("Database was created with a newer version: $version >= ${SqlStorage.VERSION}")
        }

        if (version == 0) {
            executeBatch("schema")
        } else {
            transaction {
                for (version in (version + 1)..SqlStorage.VERSION) {
                    try {
                        executeBatch("migrations/${version}")
                    } catch (error: Throwable) {
                        throw IOException("Error during database migration $version: ${error.message}", error)
                    }
                }
            }
        }

        execute("PRAGMA user_version = ${SqlStorage.VERSION};")
    }


    private fun PreparedStatement.bind(vararg parameters: Any?) {
        for ((index, parameter) in parameters.withIndex()) {
            val actual = index + 1
            when (parameter) {
                null -> setNull(actual, Types.NULL)
                is Int -> setInt(actual, parameter)
                is Long -> setLong(actual, parameter)
                is String -> setString(actual, parameter)
                is Instant -> setLong(actual, parameter.epochSeconds)
                is Uuid -> setString(actual, parameter.toString())
                is ByteArray -> setBytes(actual, parameter)
                is Enum<*> -> setString(actual, parameter.name)
                else -> throw IllegalArgumentException("Unknown parameter type: $parameter")
            }
        }
    }

    private fun createStatement(sql: String, vararg parameters: Any?): PreparedStatement {
        val statement = connection.prepareStatement(sql)
        statement.bind(*parameters)

        return statement
    }


    override fun query(sql: String, vararg parameters: Any?): SQLiteHelper.Cursor {
        val statement = createStatement(sql, *parameters)

        return SqlCursor(statement.executeQuery())
    }

    override fun execute(sql: String, vararg parameters: Any?): Int {
        val statement = createStatement(sql, *parameters)
        return statement.use { it.executeUpdate() }
    }

    override fun insert(sql: String, vararg parameters: Any?): Long {
        val statement = createStatement(sql, *parameters)

        return statement.use { it.executeUpdate().toLong() } // TODO: return auto increment id
    }

    override fun <T> transaction(block: () -> T): T {
        connection.autoCommit = false
        try {
            val result = block.invoke()
            connection.commit()
            return result
        } catch (error: Throwable) {
            connection.rollback()
            throw error
        } finally {
            connection.autoCommit = true
        }
    }

    override fun close() {
        connection.close()
    }

    class SqlCursor(val cursor: ResultSet) : SQLiteHelper.Cursor {
        override fun getBlob(index: Int) = cursor.getBytes(index +1)
        override fun getBlobOrNull(index: Int) = cursor.getBytes(index+1)

        override fun getString(index: Int) = cursor.getString(index+1)
        override fun getStringOrNull(index: Int) = cursor.getString(index+1)

        override fun getInt(index: Int) = cursor.getInt(index+1)
        override fun getLong(index: Int) = cursor.getLong(index+1)

        override fun isNull(index: Int) = false // TODO

        override fun moveToNext() = cursor.next()

        override fun close() = cursor.close()

        override fun isEmpty() = !cursor.next()
    }
}
