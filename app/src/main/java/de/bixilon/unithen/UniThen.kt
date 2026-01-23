package de.bixilon.unithen

import android.app.Application
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.sql.SqlStorage

class UniThen : Application() {

    override fun onCreate() {
        super.onCreate()
        DataStorage.STORAGE = SqlStorage(applicationContext)
    }
}
