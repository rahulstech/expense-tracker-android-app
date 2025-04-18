package dreammaker.android.expensetracker.ui.history.viewhistory

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.databinding.ViewHistoryBinding

class MyAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {

    private val TAG = MyAdapter::class.simpleName

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        Log.i(TAG, "create fragment for position $position")
        val date = getDateForPosition(position)
        val fragment = ViewDayHistoryFragment(date)
        return fragment
    }

    fun getDateForPosition(position: Int): Date {
        val days = position - getTodayPosition()
        val date = Date().plusDays(days)
        Log.i(TAG, "getDateForPosition: position=$position date=$date")
        return date
    }

    fun getPositionForDate(date: Date): Int {
        val today = Date()
        val days = Date.durationDays(today, date)
        val position = getTodayPosition() + days
        Log.i(TAG,"getPositionForDate: date=$date days-difference-today=$days position=$position")
        return position
    }

    fun getTodayPosition(): Int = itemCount/2+1
}

class ViewHistory: Fragment() {

    lateinit var binding: ViewHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = MyAdapter(requireActivity())
        binding.historyViewPager.adapter = adapter

        val initialDate = Date(2025,3,5)
        binding.historyViewPager.currentItem = adapter.getPositionForDate(initialDate)
        binding.btnDatePicker.text = initialDate.toString()

        binding.historyViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val date = adapter.getDateForPosition(position)
                binding.btnDatePicker.text = date.toString()
            }
        })

        binding.btnDatePicker.setOnClickListener {
            val currentDate = adapter.getDateForPosition(binding.historyViewPager.currentItem)
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
                val date = Date(year,month,day)
                binding.historyViewPager.currentItem = adapter.getPositionForDate(date)
            }, currentDate.year, currentDate.month, currentDate.dayOfMonth)
            datePicker.show()
        }
    }
}