package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.unithen.api.UserDetails
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.util.*

class AccountTable(
    storage: SqlStorage,
) : SqlTable<Account>(storage, "accounts") {
    override val columns = listOf("id", "site", "uuid", "firstname", "lastname", "session_key")

    override fun map(cursor: Cursor) = Account(cursor.getInt(0), cursor.getInt(1), cursor.getString(3).toUUID(), cursor.getString(4), cursor.getString(5), cursor.getString(6))

    operator fun get(id: Key) = single("id=?", id.toString())

    fun get(site: Site, uuid: UUID) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))
    fun get(site: Site? = null, uuid: UUID? = null, firstname: String? = null, lastname: String? = null, sessionKey: String? = null): List<Account> {
        return all(SqlFilter.and("site" to site, "uuid" to uuid, "firstname" to firstname, "lastname" to lastname, "session_key" to sessionKey))
    }

    operator fun get(site: Site) = single("site=?", site.id.toString())


    fun update(id: Int, firstname: String? = null, lastname: String? = null, sessionKey: String? = null) {
        TODO()
    }


    fun update(site: Site, details: UserDetails, authentication: Authentication) {
    }
}
