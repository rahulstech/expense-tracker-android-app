package dreammaker.android.expensetracker.ui.history.viewhistory

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.databinding.ViewHistoryBinding

class DailyHistoryFragmentAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {

    private val TAG = DailyHistoryFragmentAdapter::class.simpleName

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        Log.i(TAG, "create fragment for position $position")
        val date = getDateForPosition(position)
        val fragment = ViewDayHistoryFragment(date)
        return fragment
    }

    fun getDateForPosition(position: Int): Date {
        val days = position - getPositionForToday()
        val date = Date().plusDays(days)
        Log.i(TAG, "getDateForPosition: position=$position date=$date")
        return date
    }

    fun getPositionForDate(date: Date): Int {
        val today = Date()
        val days = Date.durationDays(today, date)
        val position = getPositionForToday() + days
        Log.i(TAG,"getPositionForDate: date=$date days-difference-today=$days position=$position")
        return position
    }

    fun getPositionForToday(): Int = itemCount/2+1
}

class DailyViewHistoryFragment: Fragment() {

    private val TAG = DailyViewHistoryFragment::class.simpleName

    private val DATE_FORMAT = "EEE, dd-MMM-yyyy"

    private lateinit var binding: ViewHistoryBinding

    private lateinit var adapter: DailyHistoryFragmentAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.view_history_menu,menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = DailyHistoryFragmentAdapter(requireActivity())
        binding.historyViewPager.adapter = adapter

        val today = Date()
        binding.historyViewPager.currentItem = adapter.getPositionForDate(today)
        changeCurrentDateText(today)

        binding.historyViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val currentDate = getCurrentDate()
                changeCurrentDateText(currentDate)
            }
        })

        binding.btnDatePicker.setOnClickListener {
            val currentDate = getCurrentDate()
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
                val date = Date(year,month,day)
                binding.historyViewPager.currentItem = adapter.getPositionForDate(date)
            }, currentDate.year, currentDate.month, currentDate.dayOfMonth)
            datePicker.show()
        }
    }

    private fun changeCurrentDateText(date: Date) {
        binding.btnDatePicker.text = date.format(DATE_FORMAT)
    }

    private fun getCurrentDate(): Date = adapter.getDateForPosition(binding.historyViewPager.currentItem)
}