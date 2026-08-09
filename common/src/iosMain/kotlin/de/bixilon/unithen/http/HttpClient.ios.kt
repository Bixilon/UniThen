package de.bixilon.unithen.http

import platform.Network.*
import platform.darwin.dispatch_get_global_queue
import platform.posix.QOS_CLASS_BACKGROUND

private var path: nw_path_t = null
private val MONITOR by lazy {
    nw_path_monitor_create().apply {
        nw_path_monitor_set_queue(this, dispatch_get_global_queue(QOS_CLASS_BACKGROUND.toLong(), 0u))
        nw_path_monitor_set_update_handler(this) { path = it }
        nw_path_monitor_start(this)
    }
}

actual fun hasNetwork(): Boolean {
    MONITOR // init lazy
    return path == null || nw_path_get_status(path) == nw_path_status_satisfied
}
