package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import dreammaker.android.expensetracker.core.util.durationMonthsTill
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryPageAdapter
import dreammaker.android.expensetracker.util.putMonthYear
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MonthlyHistoryFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): ViewHistoryPageAdapter<YearMonth>(fragmentManager,lifecycle) {

    companion object {
        private val TAG = MonthlyHistoryFragmentAdapter::class.simpleName
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("MMM-yyyy")
    }

    override fun getItemCount(): Int = 1000

    override fun getPresentData(): YearMonth = YearMonth.now()

    override fun plusDelta(data: YearMonth, delta: Int): YearMonth = data.plusMonths(delta.toLong())

    override fun calculateDifference(from: YearMonth, to: YearMonth): Int = from.durationMonthsTill(to).toInt()

    override fun getDataLabel(data: YearMonth): CharSequence = data.format(DATE_FORMAT)

    override fun onCreateFragment(position: Int, data: YearMonth, arguments: Bundle?): Fragment {
        val fragment = ViewMonthHistoryFragment()
        fragment.arguments = Bundle().apply {
            putMonthYear(ViewMonthHistoryFragment.ARG_MONTH_YEAR, data)
        }
        return fragment
    }
}
