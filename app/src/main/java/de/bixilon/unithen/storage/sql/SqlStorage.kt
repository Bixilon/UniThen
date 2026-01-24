package de.bixilon.unithen.storage.sql

import android.content.Context
import android.database.Cursor
import androidx.core.database.sqlite.transaction
import de.bixilon.unithen.storage.DataStorage
import okio.Closeable
import org.intellij.lang.annotations.Language

class SqlStorage(context: Context) : DataStorage, Closeable {
    private val helper = SqlHelper(context)
    private val database = helper.writableDatabase

    val sites = SiteTable(this)
    val accounts = AccountTable(this)
    val courses = CourseTable(this)

    fun <T> query(@Language("SQL") sql: String, vararg parameters: String, runnable: (Cursor) -> T): T {
        return database.rawQuery(sql, parameters).use { runnable.invoke(it) }
    }
    fun execute(@Language("SQL") sql: String, vararg parameters: String) {
        database.execSQL(sql, parameters)
    }


    fun <T> transaction(block: (SqlStorage) -> T) = database.transaction { block.invoke(this@SqlStorage) }

    override fun close() {
        database.close()
        helper.close()
    }
}
