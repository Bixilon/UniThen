package de.bixilon.unithen.storage.android

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SqlHelper(context: Context) : SQLiteOpenHelper(context, NAME, null, VERSION) {


    override fun onCreate(p0: SQLiteDatabase?) {
        TODO("Not yet implemented")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        const val NAME = "uninow"
        const val VERSION = 1
    }
}
