package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import android.os.Bundle
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.ui.history.historieslist.BaseViewHistoryFragment
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryPageAdapter
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.util.putDate
import dreammaker.android.expensetracker.util.MonthYear

class MonthlyViewHistoryFragment: BaseViewHistoryFragment<MonthYear>() {
    private val TAG = MonthlyViewHistoryFragment::class.simpleName

    override fun onCreatePageAdapter(): ViewHistoryPageAdapter<MonthYear> {
        return MonthlyHistoryFragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
    }

    override fun getGotoPresentButtonText(): CharSequence = getString(R.string.label_this_month)

    override fun onClickDataPicker(currentData: MonthYear) {
        val monthPicker = MonthPickerDialog(requireContext()).apply {
            minMonthYear = adapter.getMinData()
            maxMonthYear = adapter.getMaxData()
            updateMonthYear(currentData)
            monthChangeListener = { _, month,year ->
                val monthYear = MonthYear(month,year)
                setCurrentData(monthYear)
            }
        }
        monthPicker.show()
    }

    override fun onPutCreateHistoryArgument(type: HistoryType, argument: Bundle) {
        argument.apply {
            putDate(HistoryInputFragment.ARG_HISTORY_DATE, getCurrentData()!!.toFirstDate())
        }
    }
}