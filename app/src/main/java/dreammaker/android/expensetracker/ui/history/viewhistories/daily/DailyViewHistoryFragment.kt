package dreammaker.android.expensetracker.ui.history.viewhistories.daily

import android.app.DatePickerDialog
import android.os.Bundle
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.history.viewhistories.BaseViewHistoryFragment
import dreammaker.android.expensetracker.ui.history.viewhistories.ViewHistoryPageAdapter
import dreammaker.android.expensetracker.ui.util.putDate

class DailyViewHistoryFragment: BaseViewHistoryFragment<Date>() {

    companion object {
        private val TAG = DailyViewHistoryFragment::class.simpleName
    }


    override fun getPageAdapter(): ViewHistoryPageAdapter<Date> {
        return DailyHistoryFragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
    }

    override fun getGotoPresentButtonText(): CharSequence = getString(R.string.today)

    override fun onClickDataPicker(currentData: Date?) {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val date = Date(year,month,day)
            setCurrentData(date)
        }, currentData!!.year, currentData.month, currentData.dayOfMonth)
        datePicker.show()
    }

    override fun onPutCreateHistoryArgument(type: HistoryType, argument: Bundle) {
        argument.apply {
            putDate(HistoryInputFragment.ARG_HISTORY_DATE, getCurrentData())
        }
    }
}