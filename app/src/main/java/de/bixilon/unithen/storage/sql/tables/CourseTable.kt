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

package de.bixilon.unithen.storage.sql.tables

import android.database.Cursor
import de.bixilon.unithen.storage.*
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getInstant
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.util.*
import kotlin.time.Instant

class CourseTable(
    storage: SqlStorage,
) : SqlTable<Course>(storage, "courses") {
    override val columns = listOf("id", "site", "event", "uuid", "name", "fetched")

    override fun map(cursor: Cursor) = Course(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getUUID(3), cursor.getString(4), cursor.getInstant(5))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(site: Site, uuid: UUID) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))

    fun get(site: Site? = null, event: Event? = null, uuid: UUID? = null, name: String? = null) = all(SqlFilter.and("site" to site?.id, "event" to event?.id, "uuid" to uuid, "name" to name))

    fun update(id: Key, name: String? = null, fetched: Instant? = null) = update(id, SqlFilter.comma("name" to name, "fetched" to fetched))


    fun insert(site: Site, event: Event, uuid: UUID, name: String, fetched: Instant): Course {
        val id = insert("INSERT INTO $table(site, event, uuid, name, fetched) VALUES (?,?,?,?,?)", site.id, event.id, uuid, name, fetched)

        return this[id]!! // TODO: cleanup
    }

    fun add(site: Site, event: Event, uuid: UUID, name: String, fetched: Instant): Course {
        this[site, uuid]?.let { update(it.id, name, fetched); return it }

        return insert(site, event, uuid, name, fetched)
    }


    fun clearTutors(course: Course) = update("DELETE FROM tutor_courses WHERE course = ?", course.id)
    fun addTutor(user: User, course: Course) {
        insert("INSERT INTO tutor_courses(user, course) VALUES (?,?) ON CONFLICT(user, course) DO NOTHING", user.id, course.id)
        notifyState()
    }

    operator fun get(account: Account): List<Course> {
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table INNER JOIN account_courses ON account_courses.course = $table.id WHERE account = ?", account.id) { it.collectAll() }
    }
}
