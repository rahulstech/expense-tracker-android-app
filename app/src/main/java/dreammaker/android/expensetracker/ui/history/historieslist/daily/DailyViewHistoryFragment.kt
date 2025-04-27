package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.core.os.BundleCompat
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.ui.history.historieslist.BaseViewHistoryFragment
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryPageAdapter
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.util.AccountModelParcel
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.GroupModelParcel
import dreammaker.android.expensetracker.ui.util.putDate

class DailyViewHistoryFragment: BaseViewHistoryFragment<Date>() {
    private val TAG = DailyViewHistoryFragment::class.simpleName

    override fun onCreatePageAdapter(): ViewHistoryPageAdapter<Date> {
        val adapter = DailyHistoryFragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        val args = getModelArgument()
        args?.let { adapter.putArguments(it) }
        return adapter
    }

    override fun getGotoPresentButtonText(): CharSequence = getString(R.string.today)

    override fun onClickDataPicker(currentData: Date) {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val date = Date(year,month,day)
            setCurrentData(date)
        }, currentData.year, currentData.month, currentData.dayOfMonth)
        datePicker.show()
    }

    override fun onPutCreateHistoryArgument(type: HistoryType, argument: Bundle) {
        argument.apply {
            putDate(HistoryInputFragment.ARG_HISTORY_DATE, getCurrentData())
            val args = getModelArgument()
            args?.let { argument.putAll(args) }
        }
    }

    private fun getModelArgument(): Bundle? {
        return arguments?.let {
            val account = BundleCompat.getParcelable(it,Constants.ARG_ACCOUNT,AccountModelParcel::class.java)
            val group = BundleCompat.getParcelable(it,Constants.ARG_GROUP,GroupModelParcel::class.java)
            val args = Bundle().apply {
                account?.let { putParcelable(Constants.ARG_ACCOUNT, account) }
                group?.let { putParcelable(Constants.ARG_GROUP, group) }
            }
            args
        }
    }
}