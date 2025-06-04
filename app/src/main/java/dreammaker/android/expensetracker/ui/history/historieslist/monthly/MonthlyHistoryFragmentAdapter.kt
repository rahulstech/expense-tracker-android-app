package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryPageAdapter
import dreammaker.android.expensetracker.util.putMonthYear
import dreammaker.android.expensetracker.util.MonthYear

class MonthlyHistoryFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): ViewHistoryPageAdapter<MonthYear>(fragmentManager,lifecycle) {

    companion object {
        private val TAG = MonthlyHistoryFragmentAdapter::class.simpleName
        private const val DATE_FORMAT = "MMM-yyyy"
    }

    override fun getItemCount(): Int = 1000

    override fun getPresentData(): MonthYear = MonthYear.now()

    override fun plusDelta(data: MonthYear, delta: Int): MonthYear = data.plusMonths(delta)

    override fun calculateDifference(from: MonthYear, to: MonthYear): Int = MonthYear.durationMonths(from, to)

    override fun getDataLabel(data: MonthYear): CharSequence = data.format(DATE_FORMAT)

    override fun onCreateFragment(position: Int, data: MonthYear, arguments: Bundle?): Fragment {
        val fragment = ViewMonthHistoryFragment()
        fragment.arguments = Bundle().apply {
            putMonthYear(ViewMonthHistoryFragment.ARG_MONTH_YEAR, data)
        }
        return fragment
    }
}
