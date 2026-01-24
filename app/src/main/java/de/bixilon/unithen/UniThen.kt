package de.bixilon.unithen

import android.app.Application
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.sql.SqlStorage

class UniThen : Application() {

    override fun onCreate() {
        super.onCreate()
        DataStorage.STORAGE = SqlStorage(applicationContext)

        DataStorage.STORAGE.accounts.all().forEach {
            val site = DataStorage.STORAGE.sites[it.site]!!
            val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(it.session))
            DefaultThreadPool += {
                val courses = api.postings(it.uuid)

                DataStorage.STORAGE.populate(site, it, courses)
            }

        }
    }

    override fun onTerminate() {
        super.onTerminate()
        DataStorage.STORAGE.close()
    }
}
