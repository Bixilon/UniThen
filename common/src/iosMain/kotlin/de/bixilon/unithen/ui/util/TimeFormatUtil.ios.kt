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

import androidx.compose.runtime.Composable
import de.bixilon.unithen.ui.util.TimeFormatUtil.formatTime
import kotlinx.datetime.LocalDateTime
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale


fun is12HourFormat(): Boolean {
    val dateFormat = NSDateFormatter.dateFormatFromTemplate("j", 0u, locale = NSLocale.currentLocale) ?: return false

    return dateFormat.contains("a")
}

@Composable
actual fun LocalDateTime.formatTime() = formatTime(!is12HourFormat())

@Composable
actual fun LocalDateTime.formatDate() = formatDate(NSLocale.currentLocale)


@Composable
fun LocalDateTime.formatDate(locale: NSLocale): String {
    return this.toString() // TODO
}
