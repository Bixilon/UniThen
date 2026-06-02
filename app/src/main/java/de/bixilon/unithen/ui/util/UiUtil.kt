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

import de.bixilon.kutil.unit.UnitFormatter.format
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

object UiUtil {
    private val MONTHS_GERMAN = MonthNames("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember")
    private val DAYS_GERMAN = DayOfWeekNames("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
    val DATE_FORMAT_ENGLISH = LocalDateTime.Format { dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED); chars(", "); monthName(MonthNames.ENGLISH_FULL); char(' '); day(); char(' '); year(); chars(" - "); amPmHour(); char(':'); minute(); char(' '); amPmMarker("AM", "PM") }
    val DATE_FORMAT_GERMAN = LocalDateTime.Format { dayOfWeek(DAYS_GERMAN); chars(", "); day(); chars(". "); monthName(MONTHS_GERMAN); char(' '); year(); chars(" - "); hour(); char(':'); minute() }

    val DATE_FORMAT = if (Locale.getDefault() == Locale.GERMAN) DATE_FORMAT_GERMAN else DATE_FORMAT_ENGLISH

    fun Instant.format() = this.toLocalDateTime(TimeZone.currentSystemDefault()).format(DATE_FORMAT)
    fun Instant.formatNow(): String {
        if (this.epochSeconds == 0L) return "never"
        val delta = Clock.System.now() - this

        return (if (delta > Duration.ZERO) "in " else "") + delta.absoluteValue.format() + (if (delta < Duration.ZERO) " ago" else "")
    }
}
