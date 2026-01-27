/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

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
        val id = insert("INSERT INTO $table(site, uuid, name) VALUES (?,?,?)", site.id, uuid, name)

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
