package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.util.*

class CourseTable(
    storage: SqlStorage,
) : SqlTable<Course>(storage, "courses") {
    override val columns = listOf("id", "site", "uuid", "name")

    override fun map(cursor: Cursor) = Course(cursor.getInt(0), cursor.getInt(1), cursor.getString(2).toUUID(), cursor.getString(3))

    operator fun get(id: Key) = single("id=?", id.toString())
    operator fun get(site: Site, uuid: UUID) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))

    fun get(site: Site? = null, uuid: UUID? = null, name: String? = null) = all(SqlFilter.and("site" to site, "uuid" to uuid, "name" to name))

    fun update(id: Key, name: String? = null) = update(id, SqlFilter.comma("name" to name))


    fun insert(site: Site, uuid: UUID, name: String) {
        storage.execute("INSERT INTO $table(site, uuid, name) VALUES (?,?,?,?,?)", site.id.toString(), uuid.toString(), name)
    }

    fun add(site: Site, uuid: UUID, name: String) {
        this[site, uuid]?.let { return update(it.id, name) }

        insert(site, uuid, name)
    }
}
