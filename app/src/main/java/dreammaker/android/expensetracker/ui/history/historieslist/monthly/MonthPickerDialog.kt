package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.view.get
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.MonthYearPickerLayoutBinding
import java.time.YearMonth
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class YearAdapter(context: Context) : ArrayAdapter<String>(context, R.layout.month_picker_year_view) {

    private var yearStart = 2000
    private var yearEnd = 2000

    init {
        setDropDownViewResource(android.R.layout.simple_list_item_1)
    }

    fun updateYearRange(minYear: Int, maxYear: Int) {
        yearStart = minYear
        yearEnd = maxYear
        notifyDataSetChanged()
    }

    override fun getCount(): Int = yearEnd - yearStart + 1

    override fun getItem(position: Int): String =
        String.format(Locale.ENGLISH, "%4d", getYear(position))

    fun getYear(position: Int): Int = yearStart + position

    override fun getItemId(position: Int): Long = getYear(position).toLong()
}

typealias MonthChangeListener = (MonthPickerDialog, Int, Int)->Unit

class MonthPickerDialog(context: Context) : AlertDialog(context), DialogInterface.OnClickListener {

    private val chipIdToMonth = mapOf(
        R.id.month_january to 0,
        R.id.month_february to 1,
        R.id.month_march to 2,
        R.id.month_april to 3,
        R.id.month_may to 4,
        R.id.month_june to 5,
        R.id.month_july to 6,
        R.id.month_august to 7,
        R.id.month_september to 8,
        R.id.month_october to 9,
        R.id.month_november to 10,
        R.id.month_december to 11,
    )

    private val binding = MonthYearPickerLayoutBinding.inflate(LayoutInflater.from(context))
    private val adapter = YearAdapter(context)

    private var monthYear = YearMonth.now()

    var minMonthYear: YearMonth = YearMonth.now().plusYears(-10)
        set(value) { if (!isShowing) field = value }

    var maxMonthYear: YearMonth = YearMonth.now().plusYears(10)
        set(value) { if (!isShowing) field = value }

    var monthChangeListener: MonthChangeListener? = null

    init {
        setView(binding.root)
        setupYearPicker()
        setupMonthLayout()
        setupButtons()
    }

    private fun setupYearPicker() {
        binding.inputYear.adapter = adapter
        binding.inputYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                handleYearChange(adapter.getYear(position))
            }

            override fun onNothingSelected(parent: AdapterView<*>) { /* no-op */}
        }
    }

    private fun setupMonthLayout() {
        binding.monthsLayout.setOnCheckedStateChangeListener { _, _ ->
            handleMonthChange(binding.monthsLayout.checkedChipId)
        }
    }

    private fun setupButtons() {
        binding.btnYearDecrease.setOnClickListener { changeYear(-1) }
        binding.btnYearIncrease.setOnClickListener { changeYear(1) }

        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this)
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this)
    }

    override fun onStart() {
        super.onStart()
        adapter.updateYearRange(minMonthYear.year, maxMonthYear.year)
        showMonthPickerView(monthYear)
    }

    private fun handleYearChange(year: Int) {
        val old = monthYear
        monthYear = YearMonth.of(year, monthYear.month)
        binding.btnYearDecrease.visibility = if (year > minMonthYear.year) View.VISIBLE else View.GONE
        binding.btnYearIncrease.visibility = if (year < maxMonthYear.year) View.VISIBLE else View.GONE
        binding.monthsLayout.apply {
            children.forEachIndexed { index, child ->
                child.isEnabled = when (year) {
                    minMonthYear.year -> index >= minMonthYear.month.value
                    maxMonthYear.year -> index <= maxMonthYear.month.value
                    else -> true
                }
            }

            val selectedMonthChip = this[old.month.value]
            if (!selectedMonthChip.isEnabled) {
                when (year) {
                    minMonthYear.year -> check(this[minMonthYear.month.value].id)
                    maxMonthYear.year -> check(this[maxMonthYear.month.value].id)
                }
            }
        }
    }

    private fun changeYear(delta: Int) {
        val newSelection = (binding.inputYear.selectedItemPosition + delta)
            .coerceIn(0, adapter.count - 1)
        binding.inputYear.setSelection(newSelection)
    }

    private fun handleMonthChange(checkedId: Int) {
        val month = chipIdToMonth[checkedId]
            ?: throw IllegalArgumentException("Unknown month chip id: $checkedId") // this exception must never be thrown
        monthYear = YearMonth.of(monthYear.year,month)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            BUTTON_POSITIVE -> monthChangeListener?.invoke(this, monthYear.month.value, monthYear.year)
            BUTTON_NEGATIVE -> cancel()
        }
    }

    private fun showMonthPickerView(monthYear: YearMonth) {
        val year = monthYear.year
        val month = monthYear.month.value
        val delta = year-minMonthYear.year
        changeYear(delta)
        val chipIndex = when(year) {
            minMonthYear.year -> max(month,minMonthYear.month.value)
            maxMonthYear.year -> min(month, maxMonthYear.month.value)
            else -> month
        }
        val chip = binding.monthsLayout[chipIndex]
        binding.monthsLayout.check(chip.id)
    }
    /**
     * month is 0 (zero) bases 0 = January 11 = December
     */
    fun updateMonthYear(month: Int, year: Int) {
        updateMonthYear(YearMonth.of(year,month))
    }

    fun updateMonthYear(monthYear: YearMonth) {
        this.monthYear = monthYear
        if (isShowing) {
            showMonthPickerView(monthYear)
        }
    }
}
