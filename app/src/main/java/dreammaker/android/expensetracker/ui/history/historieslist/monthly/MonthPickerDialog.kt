package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.MonthYearPickerLayoutBinding
import dreammaker.android.expensetracker.util.MonthYear
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class YearAdapter(context: Context, val yearStart: Int, val yearEnd: Int): ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item) {

    override fun getCount(): Int = yearEnd - yearStart + 1

    override fun getItem(position: Int): String = String.format(Locale.ENGLISH, "%4d", getYear(position))

    fun getAdjustedYearPosition(year: Int): Int {
        val diff = year-yearStart
        return min(max(diff, count-1), 0)
    }

    fun getYear(position: Int): Int = yearStart+position

    override fun getItemId(position: Int): Long = (yearStart+position).toLong()
}

typealias MonthChangeListener = (picker: MonthPickerDialog, month: Int, year: Int)->Unit

class MonthPickerDialog(context: Context): AlertDialog(context), DialogInterface.OnClickListener {

    private val binding: MonthYearPickerLayoutBinding

    private var adapter: YearAdapter

    private var monthYear = MonthYear.now()

    var minMonthYear = MonthYear.now().plusYears(-10)
        set(monthYear) {
            if(!isShowing) {
                field = monthYear
            }
        }

    var maxMonthYear = MonthYear.now().plusYears(10)
        set(monthYear) {
            if (!isShowing) {
                field = monthYear
            }
        }

    var monthChangeListener: MonthChangeListener? = null

    init {
        val themeContext = getContext()
        val inflater = LayoutInflater.from(themeContext)
        binding = MonthYearPickerLayoutBinding.inflate(inflater)
        setView(binding.root)

        adapter = YearAdapter(themeContext, minMonthYear.year, maxMonthYear.year)
        binding.inputYear.adapter = adapter

        updateMonthYear(monthYear)
        binding.inputYear.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                handleYearChange(adapter.getYear(position))
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.monthsLayout.setOnCheckedStateChangeListener { _,_ -> handleMonthChange(binding.monthsLayout.checkedChipId) }
        binding.btnYearDecrease.setOnClickListener { handleYearDecrease() }
        binding.btnYearIncrease.setOnClickListener { handleYearIncrease() }

        setButton(BUTTON_POSITIVE, themeContext.getString(android.R.string.ok), this)
        setButton(BUTTON_NEGATIVE, themeContext.getString(android.R.string.cancel), this)
    }

    private fun handleYearChange(year: Int) {
        val old = monthYear
        monthYear = MonthYear(old.month,year)
    }

    private fun handleYearIncrease() {
        val currentSelection = binding.inputYear.selectedItemPosition
        val newSelection = min(adapter.count-1, currentSelection+1)
        binding.inputYear.setSelection(newSelection)
    }

    private fun handleYearDecrease() {
        val currentSelection = binding.inputYear.selectedItemPosition
        val newSelection = max(0, currentSelection-1)
        binding.inputYear.setSelection(newSelection)
    }

    private fun handleMonthChange(checkedId: Int) {
        val old = monthYear
        val month = when(checkedId) {
            R.id.month_january -> 0
            R.id.month_february -> 1
            R.id.month_march -> 2
            R.id.month_april -> 3
            R.id.month_may -> 4
            R.id.month_june -> 5
            R.id.month_july -> 6
            R.id.month_august -> 7
            R.id.month_september -> 8
            R.id.month_october -> 9
            R.id.month_november -> 10
            R.id.month_december -> 11
            else -> throw IllegalArgumentException()
        }
        monthYear = MonthYear(month, old.year)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when(which) {
            BUTTON_POSITIVE -> {
                monthChangeListener?.let { listener ->
                    binding.root.clearFocus()
                    listener.invoke(this,monthYear.month,monthYear.year)
                }
            }
            BUTTON_NEGATIVE -> {
                cancel()
            }
        }
    }

    /**
     * month is 0 (zero) based; i.e. 0 = January 11 = December
     */
    fun updateMonthYear(month: Int, year: Int) {
        val yearPosition = adapter.getAdjustedYearPosition(year)
        binding.inputYear.setSelection(yearPosition)
        val monthChip = binding.monthsLayout.get(month)
        binding.monthsLayout.check(monthChip.id)
    }

    fun updateMonthYear(monthYear: MonthYear) {
        updateMonthYear(monthYear.month, monthYear.year)
    }

    override fun onSaveInstanceState(): Bundle {
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }


}