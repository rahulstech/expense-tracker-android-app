package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryPageAdapter
import dreammaker.android.expensetracker.util.putDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DailyHistoryFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle)
    : ViewHistoryPageAdapter<LocalDate>(fragmentManager,lifecycle) {

    companion object {
        private val TAG = DailyHistoryFragmentAdapter::class.simpleName
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy")
    }

    override fun getItemCount(): Int = 10000

    override fun getPresentData(): LocalDate = LocalDate.now()

    override fun plusDelta(data: LocalDate, delta: Int): LocalDate = data.plusDays(delta.toLong())

    override fun calculateDifference(from: LocalDate, to: LocalDate): Int =
        ChronoUnit.DAYS.between(from,to.plusDays(1)).toInt()

    override fun getDataLabel(data: LocalDate): CharSequence = data.format(DATE_FORMAT)

    override fun onCreateFragment(position: Int, data: LocalDate, arguments: Bundle?): Fragment {
        val fragment = ViewDayHistoryFragment()
        fragment.arguments = Bundle().apply {
            putDate(ViewDayHistoryFragment.ARG_DATE, data)
        }
        return fragment
    }
}
