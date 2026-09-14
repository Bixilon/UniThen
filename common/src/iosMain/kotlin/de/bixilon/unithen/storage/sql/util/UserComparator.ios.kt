package de.bixilon.unithen.storage.sql.util

import platform.Foundation.NSCaseInsensitiveSearch
import platform.Foundation.NSDiacriticInsensitiveSearch
import platform.Foundation.NSString
import platform.Foundation.compare

@Suppress("CAST_NEVER_SUCCEEDS")
actual fun compare(a: String, b: String): Int {
    val _a = a as NSString

    return _a.compare(b, options = NSCaseInsensitiveSearch or NSDiacriticInsensitiveSearch).toInt() // TODO: Test
}
