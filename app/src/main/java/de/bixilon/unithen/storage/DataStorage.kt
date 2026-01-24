package de.bixilon.unithen.storage

import de.bixilon.unithen.api.UserDetails
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.storage.sql.SqlStorage

interface DataStorage {

    fun updateAccount(site: Site, details: UserDetails, authentication: Authentication)

    fun <T> transaction(block: (DataStorage) -> T): T


    companion object {
        lateinit var STORAGE: SqlStorage // TODO
    }
}
