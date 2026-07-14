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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import de.bixilon.unithen.ui.util.TimeFormatUtil.formatDate
import de.bixilon.unithen.ui.util.TimeFormatUtil.formatTime
import kotlinx.datetime.LocalDateTime

@Composable
actual fun LocalDateTime.formatTime(): String {
    val context = LocalContext.current

    return formatTime(android.text.format.DateFormat.is24HourFormat(context))
}

@Composable
actual fun LocalDateTime.formatDate(): String {
    val resources = LocalResources.current

    return formatDate(resources.configuration.locales.get(0))
}
