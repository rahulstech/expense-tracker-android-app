package dreammaker.android.expensetracker.util

import dreammaker.android.expensetracker.database.Date

class MonthYear(val month: Int, val year: Int) {

    companion object {

        private const val DEFAULT_PATTERN = "yyyy-MM"

        fun now(): MonthYear = fromDate(Date())

        fun fromDate(date: Date): MonthYear {
            return MonthYear(date.month, date.year)
        }

        fun durationMonths(from: MonthYear, to: MonthYear): Int {
            return Date.durationMonths(from.toFirstDate(),to.toFirstDate())
        }

        fun valueOf(data: String, pattern: String = DEFAULT_PATTERN): MonthYear {
            val date = Date.valueOf(data,pattern)
            return date.getMonthYear()
        }
    }

    fun toFirstDate(): Date = Date(year, month, 1)

    fun toLastDate(): Date = toFirstDate().lastDateOfThisMonth()

    fun plusMonths(months: Int): MonthYear {
        val date = toFirstDate()
        return date.plusMonths(months).getMonthYear()
    }

    fun plusYears(years: Int): MonthYear {
        val date = toFirstDate()
        return date.plusYears(years).getMonthYear()
    }

    fun format(pattern: String): String {
        return toFirstDate().format(pattern)
    }

    override fun toString(): String {
        return format(DEFAULT_PATTERN)
    }
}

fun Date.getMonthYear(): MonthYear = MonthYear.fromDate(this)