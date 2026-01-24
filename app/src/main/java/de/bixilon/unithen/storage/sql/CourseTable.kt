package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.util.*

class CourseTable(
    storage: SqlStorage,
) : SqlTable<Course>(storage, "courses") {
    override val columns = listOf("id", "site", "uuid", "name")

    override fun map(cursor: Cursor) = Course(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getString(3))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(site: Site, uuid: UUID) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))

    fun get(site: Site? = null, uuid: UUID? = null, name: String? = null) = all(SqlFilter.and("site" to site, "uuid" to uuid, "name" to name))

    fun update(id: Key, name: String? = null) = update(id, SqlFilter.comma("name" to name))


    fun insert(site: Site, uuid: UUID, name: String): Course {
        val id = storage.insert("INSERT INTO $table(site, uuid, name) VALUES (?,?,?)", site.id, uuid, name)

        return this[id]!! // TODO: cleanup
    }

    fun add(site: Site, uuid: UUID, name: String): Course {
        this[site, uuid]?.let { update(it.id, name); return it }

        return insert(site, uuid, name)
    }


    fun getAccounts(account: Account): List<Course> {
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table INNER JOIN account_courses ON account_courses.course = courses.id WHERE account = ?", account.id) { it.collectAll() }
    }
}
