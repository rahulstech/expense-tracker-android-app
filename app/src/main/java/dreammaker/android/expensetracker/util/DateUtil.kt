package dreammaker.android.expensetracker.util

import dreammaker.android.expensetracker.database.Date

class MonthYear(val month: Int, val year: Int) {

    companion object {

        fun now(): MonthYear = fromDate(Date())

        fun fromDate(date: Date): MonthYear {
            return MonthYear(date.month, date.year)
        }
    }

    fun toFirstDate(): Date = Date(year, month, 1)

    fun toLastDate(): Date = toFirstDate().lastDateOfThisMonth()

    fun plusMonths(months: Long): MonthYear {
        val date = toFirstDate()
        return date.plusMonths(months).getMonthYear()
    }
}

fun Date.getMonthYear(): MonthYear = MonthYear.fromDate(this)