package de.bixilon.unithen.storage.sql.util

import de.bixilon.kutil.cast.CastUtil.cast
import platform.Foundation.NSCaseInsensitiveSearch
import platform.Foundation.NSDiacriticInsensitiveSearch
import platform.Foundation.NSString
import platform.Foundation.compare

actual fun compare(a: String, b: String): Int {
    return a.cast<NSString>().compare(b, options = NSCaseInsensitiveSearch or NSDiacriticInsensitiveSearch).toInt() // TODO: Test
}
