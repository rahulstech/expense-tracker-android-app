package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.ui.history.historieslist.BaseViewHistoryFragment
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryPageAdapter
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.util.putDate

class DailyViewHistoryFragment: BaseViewHistoryFragment<Date>() {
    private val TAG = DailyViewHistoryFragment::class.simpleName

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
    }

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
            putDate(HistoryInputFragment.ARG_HISTORY_DATE, getCurrentData())
        }
    }
}