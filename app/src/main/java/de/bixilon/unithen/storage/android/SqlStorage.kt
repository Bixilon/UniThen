package de.bixilon.unithen.storage.android

import android.content.Context
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import java.net.URI
import java.util.*

class SqlStorage(context: Context) : DataStorage {
    private val helper = SqlHelper(context)
    private val database = helper.writableDatabase


    override fun getSite(url: URI): Site {
        return database.rawQuery("SELECT id, url FROM sites WHERE url=?", arrayOf(url.host)).use { Site(it.getInt(0), URI("https:///${it.getString(1)}")) }
    }

    override fun getSites(): List<Site> {
        TODO("Not yet implemented")
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
}
