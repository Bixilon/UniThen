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
import de.bixilon.unithen.storage.Event
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.SqlTable
import de.bixilon.unithen.storage.sql.SqlUtil.getInstant
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.util.*
import kotlin.time.Instant

class EventTable(
    storage: SqlStorage,
) : SqlTable<Event>(storage, "events") {

    override val columns = listOf("id", "site", "uuid", "name", "start", "end")

    override fun map(cursor: Cursor) = Event(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getString(3), cursor.getInstant(4), cursor.getInstant(5))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(site: Site, uuid: UUID) = single(SqlFilter.and("site" to site.id, "uuid" to uuid))

    fun insert(site: Site, uuid: UUID, name: String, start: Instant, end: Instant): Event {
        val id = insert("INSERT INTO $table(site, uuid, name, start, end) VALUES (?,?,?,?)", site.id, uuid, name, start, end)

        return this[id]!!
    }

    fun add(site: Site, uuid: UUID, name: String, start: Instant, end: Instant): Event {
        this[site, uuid]?.let { return it } // TODO: update

        return insert(site, uuid, name, start, end)
    }
}
