package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.unithen.api.UserDetails
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.util.*

class AccountTable(
    storage: SqlStorage,
) : SqlTable<Account>(storage, "accounts") {
    override val columns = listOf("id", "site", "uuid", "firstname", "lastname", "session_key")

    override fun map(cursor: Cursor) = Account(cursor.getInt(0), cursor.getInt(1), cursor.getString(2).toUUID(), cursor.getString(3), cursor.getString(4), cursor.getString(5))

    operator fun get(id: Key) = single("id=?", id.toString())
    operator fun get(site: Site, uuid: UUID) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))

    fun get(site: Site? = null, uuid: UUID? = null, firstname: String? = null, lastname: String? = null, sessionKey: String? = null): List<Account> {
        return all(SqlFilter.and("site" to site, "uuid" to uuid, "firstname" to firstname, "lastname" to lastname, "session_key" to sessionKey))
    }

    fun update(id: Int, firstname: String? = null, lastname: String? = null, sessionKey: String? = null) {
        val (set, parameters) = SqlFilter.comma("firstname" to firstname, "lastname" to lastname, "session_key" to sessionKey)
        storage.execute("UPDATE $table SET $set WHERE id=?", parameters=arrayOf(*parameters.toTypedArray(), id.toString()))
    }


    private fun <X> select(filter: SqlFilter, runnable: (Cursor) -> X) = select(filter.where, arguments = filter.parameters.toTypedArray(), runnable)
    private fun <X> select(where: String = "", vararg arguments: String, runnable: (Cursor) -> X): X {
        val actualWhere = if (where.isBlank()) "" else "WHERE $where"
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table $actualWhere", *arguments, runnable = runnable)
    }

    fun update(account: Account, details: UserDetails, authentication: Authentication) {
        update(account.id, details.firstname, details.lastname, authentication.cast<CookieAuthentication>().session)
    }

    fun insert(site: Site, details: UserDetails, authentication: Authentication) {
        storage.execute("INSERT INTO $table(site, uuid, firstname, lastname, session_key) VALUES (?,?,?,?,?)", site.id.toString(), details.uuid.toString(), details.firstname, details.lastname, authentication.cast<CookieAuthentication>().session)

    }

    fun add(site: Site, details: UserDetails, authentication: Authentication) {
         this[site, details.uuid]?.let { return update(it, details, authentication) }

        insert(site, details, authentication)
    }
}
