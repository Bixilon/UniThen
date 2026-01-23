package de.bixilon.unithen.api.graphql.query

import de.bixilon.kutil.concurrent.lock.LockUtil.acquired
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import java.io.FileNotFoundException

object QueryLoader {
    private val lock = RWLock.rwlock()
    private val cache: HashMap<String, QlQuery> = HashMap()

    operator fun get(name: String): QlQuery {
        lock.acquired { this.cache[name]?.let { return it } }

        val raw = QueryLoader::class.java.getResourceAsStream("/graphql/$name.graphql") ?: throw FileNotFoundException("Can not find query: $name")

        val query = QlQuery.of(raw.readAsString())
        lock.locked { cache[name] = query }

        return query
    }
}
