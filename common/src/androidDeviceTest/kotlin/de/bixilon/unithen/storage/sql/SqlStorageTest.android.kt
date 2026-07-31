package de.bixilon.unithen.storage.sql

import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileOutputStream

actual fun ByteArray.copyTo(path: String) {
    val file = InstrumentationRegistry.getInstrumentation().context.getDatabasePath(path)
    file.parentFile!!.mkdirs()
    FileOutputStream(file).use { it.write(this) }
}

actual fun delete(path: String) {
    InstrumentationRegistry.getInstrumentation().context.getDatabasePath(path).delete()
}
