package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.ui.history.historieslist.BaseViewHistoryFragment
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryPageAdapter
import java.time.YearMonth

class MonthlyViewHistoryFragment: BaseViewHistoryFragment<YearMonth>() {
    private val TAG = MonthlyViewHistoryFragment::class.simpleName

    override fun onCreatePageAdapter(): ViewHistoryPageAdapter<YearMonth> {
        return MonthlyHistoryFragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
    }

    override fun getGotoPresentButtonText(): CharSequence = getString(R.string.label_this_month)

    override fun onClickDataPicker(currentData: YearMonth) {
        val monthPicker = MonthPickerDialog(requireContext()).apply {
            minMonthYear = adapter.getMinData()
            maxMonthYear = adapter.getMaxData()
            updateMonthYear(currentData)
            monthChangeListener = { _, month,year ->
                val monthYear = YearMonth.of(year,month)
                setCurrentData(monthYear)
            }
        }
        monthPicker.show()
    }
}