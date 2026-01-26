package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object SqlUtil {

    fun Cursor.getUUID(index: Int) = getString(index).toUUID()
    fun Cursor.getLocalDate(index: Int) = Instant.ofEpochSecond(getLong(index), 0).atZone(ZoneId.systemDefault()).toLocalDateTime()
    fun Cursor.getInstant(index: Int) = kotlin.time.Instant.fromEpochSeconds(getLong(index), 0)

    fun LocalDateTime.db() = atZone(ZoneOffset.systemDefault()).toEpochSecond()
}
