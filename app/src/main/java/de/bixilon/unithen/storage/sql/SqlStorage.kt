package de.bixilon.unithen.storage.sql

import android.content.Context
import android.database.Cursor
import androidx.core.database.sqlite.transaction
import de.bixilon.unithen.api.UserDetails
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import org.intellij.lang.annotations.Language

class SqlStorage(context: Context) : DataStorage {
    private val helper = SqlHelper(context)
    private val database = helper.writableDatabase

    val sites = SiteTable(this)

    fun <T> query(@Language("SQL") sql: String, vararg parameters: String, runnable: (Cursor) -> T): T {
        return database.rawQuery(sql, parameters).use { runnable.invoke(it) }
    }


    fun <T> _query(@Language("SQL") sql: String, vararg parameters: String, maper: (Cursor) -> T): List<T> {
        val result = ArrayList<T>()
        database.rawQuery(sql, parameters).use {
            while (it.moveToNext()) {
                result += maper.invoke(it)
            }
        }
        return result
    }

    override fun updateAccount(site: Site, details: UserDetails, authentication: Authentication) {
        TODO("Not yet implemented")
    }



    override fun <T> transaction(block: (DataStorage) -> T) = database.transaction { block.invoke(this@SqlStorage) }
}
