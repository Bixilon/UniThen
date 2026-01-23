package de.bixilon.unithen.storage.sql

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import de.bixilon.kutil.stream.InputStreamUtil.readAsString

class SqlHelper(context: Context) : SQLiteOpenHelper(context, NAME, null, VERSION) {


    override fun onCreate(database: SQLiteDatabase) {
        val schema = SqlHelper::class.java.getResourceAsStream("/sql/schema.sql")!!.readAsString().split(";").map { it.removeSuffix("\n") }.filter { it.isNotBlank() }

        database.transaction { schema.forEach { database.execSQL(it) } }
    }

    override fun onUpgrade(database: SQLiteDatabase, start: Int, end: Int) = database.transaction {
        for (version in (start + 1)..end) {
            val schema = SqlHelper::class.java.getResourceAsStream("/sql/migrations/${version}.sql")!!.readAsString()
            database.execSQL(schema)
        }
    }

    companion object {
        const val NAME = "uninow"
        const val VERSION = 1
    }
}
