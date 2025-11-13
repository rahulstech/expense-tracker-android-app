package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.app.DatePickerDialog
import android.os.Bundle
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.ui.history.historieslist.BaseViewHistoryFragment
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryPageAdapter
import dreammaker.android.expensetracker.ui.history.historyinput.TransactionInputFragment
import dreammaker.android.expensetracker.util.putDate

class DailyViewHistoryFragment: BaseViewHistoryFragment<Date>() {

    override fun onCreatePageAdapter(): ViewHistoryPageAdapter<Date> {
        return DailyHistoryFragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
    }

    override fun getGotoPresentButtonText(): CharSequence = getString(R.string.label_today)

    override fun onClickDataPicker(currentData: Date) {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val date = Date(year,month,day)
            setCurrentData(date)
        }, currentData.year, currentData.month, currentData.dayOfMonth)
        datePicker.datePicker.maxDate = adapter.getMaxData().timeInMillis
        datePicker.datePicker.minDate = adapter.getMinData().timeInMillis
        datePicker.show()
    }

    override fun onPutCreateHistoryArgument(argument: Bundle) {
        argument.apply {
            putDate(TransactionInputFragment.ARG_HISTORY_DATE, getCurrentData())
        }
    }
}