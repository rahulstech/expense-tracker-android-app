package dreammaker.android.expensetracker.ui.history.viewhistory

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.util.putDate

class DailyHistoryFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): ViewHistoryPageAdapter<Date>(fragmentManager,lifecycle) {

    companion object {
        private val TAG = DailyHistoryFragmentAdapter::class.simpleName
        private const val DATE_FORMAT = "EEE, dd-MMM-yyyy"
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun getPresentData(): Date = Date()

    override fun plusDelta(data: Date, delta: Int): Date = data.plusDays(delta)

    override fun calculateDifference(from: Date, to: Date): Int = Date.durationDays(from, to)

    override fun getDataLabel(data: Date): CharSequence = data.format(DATE_FORMAT)

    override fun onCreateFragment(position: Int, data: Date): Fragment {
        val fragment = ViewDayHistoryFragment()
        fragment.arguments = Bundle().apply { putDate(ViewDayHistoryFragment.ARG_DATE, data) }
        return fragment
    }
}

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