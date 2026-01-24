package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.Site
import java.net.URI

class SiteTable(
    storage: SqlStorage,
) : SqlTable<Site>(storage, "sites") {

    override val columns = listOf("id", "url")

    override fun map(cursor: Cursor) = Site(cursor.getInt(0), URI("https://${cursor.getString(1)}"))

    operator fun get(id: Key) = single("id=?", id.toString())
    operator fun get(url: URI) = single("url=?", url.host)
}
