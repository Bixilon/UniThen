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
import androidx.core.database.getBlobOrNull
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlUtil.getInstant
import java.net.URI
import kotlin.time.Clock

class SiteTable(
    storage: SqlStorage,
) : SqlTable<Site>(storage, "sites") {

    override val columns = listOf("id", "url", "name", "icon", "fetched")

    override fun map(cursor: Cursor) = Site(cursor.getInt(0), URI("https://${cursor.getString(1)}"), cursor.getString(2), cursor.getBlobOrNull(3), cursor.getInstant(4))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(url: URI) = single("url=?", url.host)

    fun insert(url: URI, name: String, icon: ByteArray?): Site {
        val id = insert("INSERT INTO $table(url, name, icon, fetched) VALUES (?,?,?,?)", url.host, name, icon, Clock.System.now().epochSeconds)

        return this[id]!!
    }

    fun add(url: URI, name: String, icon: ByteArray?): Site {
        this[url]?.let { return it } // TODO: update

        return insert(url, name, icon)
    }


    @Deprecated("move somewhere else")
    fun add(url: String) {
        // TODO
    }
}
