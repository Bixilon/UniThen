package de.bixilon.unithen.storage.sql.util

import de.bixilon.unithen.IOSUtil.ns
import platform.Foundation.NSCaseInsensitiveSearch
import platform.Foundation.NSDiacriticInsensitiveSearch
import platform.Foundation.compare

actual fun compare(a: String, b: String): Int {
    return a.ns().compare(b, options = NSCaseInsensitiveSearch or NSDiacriticInsensitiveSearch).toInt()
}
