package de.bixilon.unithen.storage.sql

import co.touchlab.sqliter.DatabaseFileContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import platform.posix.unlink

actual fun delete(path: String) {
    val actual = DatabaseFileContext.databasePath(path, null)
    unlink(actual)
}

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.copyTo(path: String) {
    val actual = DatabaseFileContext.databasePath(path, null)

    usePinned { pinned ->
        val fd = fopen(actual, "wb")!!
        fwrite(pinned.addressOf(0), 1u, size.toULong(), fd)
        fclose(fd)
    }
}
