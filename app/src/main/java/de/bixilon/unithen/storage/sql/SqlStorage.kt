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
import androidx.core.database.sqlite.transaction
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.unithen.api.graphql.types.CourseQl
import de.bixilon.unithen.api.graphql.types.PostingQl
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlUtil.db
import okio.Closeable
import org.intellij.lang.annotations.Language
import java.util.*
import kotlin.time.Instant

class SqlStorage(context: Context) : DataStorage, Closeable {
    private val helper = SqlHelper(context)
    val database = helper.writableDatabase

    val sites = SiteTable(this)
    val accounts = AccountTable(this)
    val courses = CourseTable(this)
    val appointments = AppointmentTable(this)

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
        return database.rawQuery(sql, parameters.map { it.db() }.toTypedArray()).use { runnable.invoke(it) }
    }

    fun insert(@Language("SQL") sql: String, vararg parameters: Any?): Int {
        val statement = database.compileStatement(sql)

        statement.bind(*parameters)

        return statement.use { it.executeInsert().toInt() }
    }

    fun update(@Language("SQL") sql: String, vararg parameters: Any?): Int {
        val statement = database.compileStatement(sql)

        statement.bind(*parameters)

        return statement.use { it.executeUpdateDelete() }
    }

    inline fun <T> transaction(block: (SqlStorage) -> T) = database.transaction { block.invoke(this@SqlStorage) }


    fun populate(site: Site, account: Account, postings: List<PostingQl>) = transaction {
        for (posting in postings) {
            val courseQl = posting.product.resource.nullCast<CourseQl>() ?: continue

            val course = courses.add(site, courseQl.id, courseQl.name)

            for (appointment in courseQl.appointments) {
                appointments.add(course, appointment.id, appointment.start, appointment.end)
            }
            accounts.addToCourse(account, course)
        }
        return@transaction
    }

    override fun close() {
        database.close()
        helper.close()
    }
}
