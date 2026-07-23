package de.bixilon.unithen.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import java.text.DateFormatSymbols
import java.util.*


@Composable
fun LocalDateTime.formatDate(locale: Locale): String {

    return remember {
        val names = DateFormatSymbols.getInstance(locale).months.toList()
        val months = MonthNames(names[Calendar.JANUARY], names[Calendar.FEBRUARY], names[Calendar.MARCH], names[Calendar.APRIL], names[Calendar.MAY], names[Calendar.JUNE], names[Calendar.JULY], names[Calendar.AUGUST], names[Calendar.SEPTEMBER], names[Calendar.OCTOBER], names[Calendar.NOVEMBER], names[Calendar.DECEMBER])
        val short = DateFormatSymbols.getInstance(locale).shortWeekdays
        val days = DayOfWeekNames(short[Calendar.MONDAY], short[Calendar.TUESDAY], short[Calendar.WEDNESDAY], short[Calendar.THURSDAY], short[Calendar.FRIDAY], short[Calendar.SATURDAY], short[Calendar.SUNDAY])


        val format = LocalDateTime.Format { dayOfWeek(days); chars(", "); day(); chars(". "); monthName(months); char(' '); year(); }


        return@remember this.format(format)
    }
}
