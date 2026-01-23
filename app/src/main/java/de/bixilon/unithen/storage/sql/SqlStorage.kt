package de.bixilon.unithen.storage.sql

import android.content.Context
import android.database.Cursor
import androidx.core.database.sqlite.transaction
import de.bixilon.unithen.api.UserDetails
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import java.net.URI
import java.util.*

class SqlStorage(context: Context) : DataStorage {
    private val helper = SqlHelper(context)
    private val database = helper.writableDatabase


    private fun <T> query(sql: String, vararg parameters: String, maper: (Cursor) -> T): List<T> {
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

    override fun createSite(url: URI): Site {
        TODO("Not yet implemented")
    }

    override fun getSite(id: Int): Site {
        TODO("Not yet implemented")
    }

    override fun getSite(url: URI): Site {
        return query("SELECT id, url FROM sites WHERE url=?", url.host) { Site(it.getInt(0), URI("https:///${it.getString(1)}")) }.first()
    }

    override fun getSites(): List<Site> {
        return query("SELECT id, url FROM sites") { Site(it.getInt(0), URI("https:///${it.getString(1)}")) }
    }

    override fun getAccount(id: Int): Account? {
        TODO("Not yet implemented")
    }

    override fun getAccount(uuid: UUID): Account? {
        TODO("Not yet implemented")
    }

    override fun getAccounts(site: Site?): List<Account> {
        TODO("Not yet implemented")
    }


    override fun <T> transaction(block: (DataStorage) -> T) = database.transaction { block.invoke(this@SqlStorage) }
}
