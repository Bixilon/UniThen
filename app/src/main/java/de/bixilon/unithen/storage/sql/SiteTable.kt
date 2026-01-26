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
