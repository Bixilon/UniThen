package de.bixilon.unithen.storage.sql

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import platform.posix.unlink

actual fun delete(path: String) {
    unlink(path)
}

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.copyTo(path: String) {
    usePinned { pinned ->
        val file = fopen(path, "wb")!!
        fwrite(pinned.addressOf(0), 1u, size.toULong(), file)
        fclose(file)
    }
}
