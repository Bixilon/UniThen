package de.bixilon.unithen.storage

import java.net.URI
import java.util.*

interface DataStorage {

    fun getSite(url: URI): Site
    fun getSites(): List<Site>

    fun getAccount(id: Int): Account?
    fun getAccount(uuid: UUID): Account?
    fun getAccounts(site: Site? = null): List<Account>
}
