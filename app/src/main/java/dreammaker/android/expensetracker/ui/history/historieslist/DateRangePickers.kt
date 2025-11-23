package dreammaker.android.expensetracker.ui.history.historieslist

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import dreammaker.android.expensetracker.FULL_DATE_FORMAT
import dreammaker.android.expensetracker.FULL_MONTH_FORM
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.atStartOfMonth
import dreammaker.android.expensetracker.core.util.getMillisInDayStart
import dreammaker.android.expensetracker.util.getDate
import dreammaker.android.expensetracker.util.putDate
import java.time.LocalDate
import java.time.YearMonth

abstract class DateRangePicker(startInclusive: LocalDate = LocalDate.now(), endInclusive: LocalDate = LocalDate.now()) {

    companion object {
        private const val KEY_DATE_START = "key_date_start"
        private const val KEY_DATE_END = "key_date_end"
    }

    var pickerCallback: ((DateRangePicker, Pair<LocalDate, LocalDate>)->Unit)? = null

    private var currentValue: Pair<LocalDate, LocalDate> = (startInclusive to endInclusive)

    abstract fun pick()

    abstract fun getSelectionLabel(): String

    fun setSelection(range: Pair<LocalDate, LocalDate>) {
        val isDifferent = range != currentValue
        updateSelection(range)
        if (isDifferent) pickerCallback?.invoke(this,getSelection())
    }

    fun selectDefault() {
        setSelection(getDefaultSelection())
    }

    abstract fun getDefaultSelection(): Pair<LocalDate, LocalDate>

    open fun updateSelection(range: Pair<LocalDate, LocalDate>) {
        currentValue = range
    }

    fun getSelection(): Pair<LocalDate, LocalDate> = currentValue

    abstract fun getDefaultLabel(): String

    fun saveState(outState: Bundle) {
        outState.apply {
            val start = currentValue.first
            val end = currentValue.second
            putDate(KEY_DATE_START, start)
            putDate(KEY_DATE_END,end)
        }
    }

    fun restoreState(savedState: Bundle?) {
        savedState?.let { bundle ->
            val start = bundle.getDate(KEY_DATE_START) ?: LocalDate.now()
            val end = bundle.getDate(KEY_DATE_END) ?: LocalDate.now()
            val range = start to end
            setSelection(range)
        }
    }
}

class DayPicker(val context: Context): DateRangePicker() {

    companion object {
        private const val NUMBER_OF_DAYS = 10000L
        private val MAX_DATE_MILLIS = LocalDate.now().getMillisInDayStart()
        private val MIN_DATE_MILLIS = LocalDate.now().minusDays(NUMBER_OF_DAYS).getMillisInDayStart()
    }

    private val DEFAULT_SELECTION = LocalDate.now() to LocalDate.now()

    private var selectedDate: LocalDate = LocalDate.now()

    override fun pick() {
        // NOTE: DatePickerDialog accepts 0 based month i.e. January = 0, February = 1 etc.,
        //       where as LocalDate month.value is 1 based i.e. January = 1, February = 2 etc.,
        val current = selectedDate
        val datePicker = DatePickerDialog(context, { _, year, month, day ->
            val date = LocalDate.of(year, month+1,day)
            setSelection(date to date)
        }, current.year, current.monthValue-1, current.dayOfMonth)
        datePicker.datePicker.maxDate = MAX_DATE_MILLIS
        datePicker.datePicker.minDate = MIN_DATE_MILLIS
        datePicker.show()
    }

    override fun getSelectionLabel(): String = selectedDate.format(FULL_DATE_FORMAT)

    override fun getDefaultLabel(): String = context.getString(R.string.label_today)

    override fun getDefaultSelection(): Pair<LocalDate, LocalDate> = DEFAULT_SELECTION

    override fun updateSelection(range: Pair<LocalDate, LocalDate>) {
        super.updateSelection(range)
        selectedDate = range.first
    }
}

class MonthPicker(val context: Context):
    DateRangePicker(
        startInclusive = YearMonth.now().atStartOfMonth(),
        endInclusive = YearMonth.now().atEndOfMonth()
    )
{

    companion object {
        private const val NUMBER_OF_MONTHS = 1000L
        private val MAX_MONTH = YearMonth.now()
        private val MIN_MONTH = YearMonth.now().minusMonths(NUMBER_OF_MONTHS)
    }

    private val DEFAULT_YEAR_MONTH = YearMonth.now()
    private val DEFAULT_SELECTION = DEFAULT_YEAR_MONTH.atStartOfMonth() to DEFAULT_YEAR_MONTH.atEndOfMonth()

    private var selectedYearMonth = YearMonth.now()

    override fun pick() {
        val monthPicker = MonthPickerDialog(context).apply {
            minMonthYear = MIN_MONTH
            maxMonthYear = MAX_MONTH
            updateMonthYear(selectedYearMonth)
            monthChangeListener = { _, month,year ->
                val monthYear = YearMonth.of(year,month)
                val start = monthYear.atStartOfMonth()
                val end = monthYear.atEndOfMonth()
                setSelection(start to end)
            }
        }
        monthPicker.show()
    }

    override fun getSelectionLabel(): String = selectedYearMonth.format(FULL_MONTH_FORM)

    override fun getDefaultLabel(): String = context.getString(R.string.label_this_month)

    override fun getDefaultSelection(): Pair<LocalDate, LocalDate> = DEFAULT_SELECTION

    override fun updateSelection(range: Pair<LocalDate, LocalDate>) {
        super.updateSelection(range)
        selectedYearMonth = YearMonth.of(range.first.year,range.first.month)
    }
}