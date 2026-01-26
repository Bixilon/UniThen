package de.bixilon.unithen.storage.sql

import android.content.Context
import android.database.Cursor
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
import java.time.LocalDateTime
import java.util.*

class SqlStorage(context: Context) : DataStorage, Closeable {
    private val helper = SqlHelper(context)
    val database = helper.writableDatabase

    val sites = SiteTable(this)
    val accounts = AccountTable(this)
    val courses = CourseTable(this)
    val appointments = AppointmentTable(this)

    fun <T> query(@Language("SQL") sql: String, vararg parameters: Any, runnable: (Cursor) -> T): T {
        return database.rawQuery(sql, parameters.map { it.toString() }.toTypedArray()).use { runnable.invoke(it) }
    }

    fun execute(@Language("SQL") sql: String, vararg parameters: Any) {
        database.execSQL(sql, parameters)
    }

    fun insert(@Language("SQL") sql: String, vararg parameters: Any?): Int {
        val statement = database.compileStatement(sql)

        for ((index, parameter) in parameters.withIndex()) {
            when (parameter) {
                null -> statement.bindNull(index + 1)
                is Int -> statement.bindLong(index + 1, parameter.toLong())
                is Long -> statement.bindLong(index + 1, parameter)
                is String -> statement.bindString(index + 1, parameter)
                is LocalDateTime -> statement.bindLong(index + 1, parameter.db())
                is UUID -> statement.bindString(index + 1, parameter.toString())
                is ByteArray -> statement.bindBlob(index + 1, parameter)
                else -> throw IllegalArgumentException("Unknown parameter type: $parameter")
            }
        }

        return statement.use { it.executeInsert().toInt() }
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
