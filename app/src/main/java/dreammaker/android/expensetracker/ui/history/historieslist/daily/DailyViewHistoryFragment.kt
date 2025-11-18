package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.app.DatePickerDialog
import android.os.Bundle
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.getMillisInDayStart
import dreammaker.android.expensetracker.ui.history.historieslist.BaseViewHistoryFragment
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryPageAdapter
import dreammaker.android.expensetracker.ui.history.historyinput.TransactionInputFragment
import dreammaker.android.expensetracker.util.putDate
import java.time.LocalDate

class DailyViewHistoryFragment: BaseViewHistoryFragment<LocalDate>() {

    override fun onCreatePageAdapter(): ViewHistoryPageAdapter<LocalDate> {
        return DailyHistoryFragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
    }

    override fun getGotoPresentButtonText(): CharSequence = getString(R.string.label_today)

    override fun onClickDataPicker(currentData: LocalDate) {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val date = LocalDate.of(year,month,day)
            setCurrentData(date)
        }, currentData.year, currentData.monthValue, currentData.dayOfMonth)
        datePicker.datePicker.maxDate = adapter.getMaxData().getMillisInDayStart()
        datePicker.datePicker.minDate = adapter.getMinData().getMillisInDayStart()
        datePicker.show()
    }

    override fun onPutCreateHistoryArgument(argument: Bundle) {
        argument.apply {
            putDate(TransactionInputFragment.ARG_HISTORY_DATE, getCurrentData())
        }
    }
}