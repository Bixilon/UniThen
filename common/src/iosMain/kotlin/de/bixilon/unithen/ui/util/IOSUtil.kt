package de.bixilon.unithen.ui.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplication


@OptIn(ExperimentalForeignApi::class)
fun forceCloseKeyboard() {
    UIApplication.sharedApplication.sendAction(NSSelectorFromString("resignFirstResponder"), to = null, from = null, forEvent = null)
}
