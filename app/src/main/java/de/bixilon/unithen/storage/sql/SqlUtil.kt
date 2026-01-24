package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.time.Instant
import kotlin.time.toJavaInstant

object SqlUtil {

    fun Cursor.getUUID(index: Int) = getString(index).toUUID()
    fun Cursor.getLocalDate(index: Int) = Instant.fromEpochSeconds(getLong(index), 0).toJavaInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

    fun LocalDateTime.db() = atZone(ZoneOffset.systemDefault())?.toEpochSecond()
}
