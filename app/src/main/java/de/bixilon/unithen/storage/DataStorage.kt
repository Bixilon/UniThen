package de.bixilon.unithen.storage

import de.bixilon.unithen.storage.sql.SqlStorage

interface DataStorage {

    //  fun <T> transaction(block: (DataStorage) -> T): T


    companion object {
        lateinit var STORAGE: SqlStorage // TODO
    }
}
