package de.bixilon.unithen

import platform.Foundation.NSString

object IOSUtil {

    @Suppress("CAST_NEVER_SUCCEEDS")
    fun String.ns() = this as NSString
}
