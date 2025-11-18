package dreammaker.android.expensetracker.core.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

fun LocalDate.getMillisInDayStart(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()

fun LocalDate.getMillisInDayEnd(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    atTime(23,59,0)
        .toInstant(ZoneOffset.of(zoneId.id))
        .toEpochMilli()

fun YearMonth.toFirstDate(): LocalDate = LocalDate.of(year,month,1)

fun YearMonth.toLastDate(): LocalDate = when {
    isLeapYear -> LocalDate.of(year,month,month.maxLength())
    else -> LocalDate.of(year,month,month.minLength())
}

fun YearMonth.durationMonthsTill(toInclusive: YearMonth): Long =
    ChronoUnit.MONTHS.between(this,toInclusive.plusMonths(1))