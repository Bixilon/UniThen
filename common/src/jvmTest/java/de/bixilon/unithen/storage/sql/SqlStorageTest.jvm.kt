package de.bixilon.unithen.storage.sql

import java.io.File
import java.io.FileOutputStream

actual fun ByteArray.copyTo(path: String) {
    FileOutputStream(path).use { it.write(this) }
}

actual fun delete(path: String) {
    File(path).delete()
}
