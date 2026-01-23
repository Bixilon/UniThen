package de.bixilon.unithen.storage

import de.bixilon.unithen.api.UserDetails
import de.bixilon.unithen.api.authentication.Authentication
import java.net.URI
import java.util.*

interface DataStorage {

    fun createSite(url: URI): Site
    fun getSite(id: Int): Site
    fun getSite(url: URI): Site
    fun getSites(): List<Site>

    fun updateAccount(site: Site, details: UserDetails, authentication: Authentication)
    fun getAccount(id: Int): Account?
    fun getAccount(uuid: UUID): Account?
    fun getAccounts(site: Site? = null): List<Account>

    fun <T> transaction(block: (DataStorage) -> T): T


    companion object {
        lateinit var STORAGE: DataStorage // TODO
    }
}
