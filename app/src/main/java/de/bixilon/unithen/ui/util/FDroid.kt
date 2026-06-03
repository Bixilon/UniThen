/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen.ui.util

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val FDROID = setOf(
    "org.fdroid.fdroid",
    "org.fdroid.basic",
    "org.gdroid.gdroid",
)

@Composable
fun rememberIsFdroid(): Boolean {
    val context = LocalContext.current.applicationContext
    try {
        val packageManager = context.packageManager
        val name = context.packageName

        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(name).installingPackageName
        } else {
            packageManager.getInstallerPackageName(name)
        }
        if (installer == null) return false

        return installer in FDROID
    } catch (_: Exception) {
    }

    return false
}
