package de.bixilon.unithen.storage.sql

import java.io.File
import java.io.FileOutputStream

actual fun ByteArray.copyTo(path: String) {
    File("databases").mkdirs()
    FileOutputStream("databases/$path").use { it.write(this) }
}

actual fun delete(path: String) {
    File("databases/$path").delete()
}
