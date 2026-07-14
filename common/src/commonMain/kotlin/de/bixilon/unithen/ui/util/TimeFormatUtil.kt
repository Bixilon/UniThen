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
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import de.bixilon.kutil.unit.UnitFormatter.format
import de.bixilon.unithen.ui.util.TimeFormatUtil.format
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.time_future
import unithen.common.generated.resources.time_never
import unithen.common.generated.resources.time_past
import java.text.DateFormatSymbols
import java.util.*
import kotlin.time.Duration
import kotlin.time.Instant

object TimeFormatUtil {

    @Composable
    private fun LocalDateTime.formatDate(): String {
        val resources = LocalResources.current

        return remember {
            val locale = resources.configuration.locales.get(0)
            val months = MonthNames(DateFormatSymbols.getInstance(locale).months.toList())
            val short = DateFormatSymbols.getInstance(locale).shortWeekdays
            val days = DayOfWeekNames(short[Calendar.MONDAY], short[Calendar.TUESDAY], short[Calendar.WEDNESDAY], short[Calendar.THURSDAY], short[Calendar.FRIDAY], short[Calendar.SATURDAY], short[Calendar.SUNDAY])


            val format = LocalDateTime.Format { dayOfWeek(days); chars(", "); day(); chars(". "); monthName(months); char(' '); year(); }


            return@remember this.format(format)
        }
    }

    @Composable
    private fun LocalDateTime.formatTime(): String {
        val context = LocalContext.current

        return remember {
            val format = when (android.text.format.DateFormat.is24HourFormat(context)) {
                true -> LocalDateTime.Format { hour(); char(':'); minute() }
                false -> LocalDateTime.Format { amPmHour(); char(':'); minute(); char(' '); amPmMarker("AM", "PM") }
            }


            return@remember this.format(format)
        }
    }


    @Composable
    fun LocalDateTime.format() = "${formatDate()} ${formatTime()}"

    @Composable
    fun Instant.format() = this.toLocalDateTime(TimeZone.currentSystemDefault()).format()

    @Composable
    fun Instant.formatNow(): String {
        if (this.epochSeconds == 0L) return Res.string.time_never.i18n()
        val delta = useTime() - this

        val format = if (delta > Duration.ZERO) Res.string.time_future else Res.string.time_past

        return format.i18n(delta.absoluteValue.format())
    }

    @Composable
    fun formatTimespam(start: Instant, end: Instant): String {
        val start = start.toLocalDateTime(TimeZone.currentSystemDefault())
        val end = end.toLocalDateTime(TimeZone.currentSystemDefault())

        if (start.day == end.day && start.month == end.month && start.year == end.year) {
            return "${start.formatDate()}: ${start.formatTime()} - ${end.formatTime()}"
        }

        return "${start.format()} - ${end.format()}"
    }
}
