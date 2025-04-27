package dreammaker.android.expensetracker.ui.history.historieslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.ViewHistoryBinding
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.isVisible
import dreammaker.android.expensetracker.ui.util.putHistoryType
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible

abstract class ViewHistoryPageAdapter<T>(fragmentManager: FragmentManager, lifecycle: Lifecycle)
    : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val TAG = ViewHistoryPageAdapter::class.simpleName

    final override fun createFragment(position: Int): Fragment {
        val date = getData(position)
        val fragment = onCreateFragment(position, date)
        return fragment
    }

    abstract fun onCreateFragment(position: Int, data: T): Fragment

    fun getData(position: Int): T {
        val delta = position - getPresentPosition()
        val data = plusDelta(getPresentData(), delta)
        return data
    }

    abstract fun getDataLabel(data: T): CharSequence

    fun getPositionForData(data: T): Int {
        val present = getPresentData()
        val delta = calculateDifference(present, data)
        val position = getPresentPosition() + delta
        return position
    }

    protected abstract fun calculateDifference(from: T, to: T): Int

    protected abstract fun plusDelta(data: T, delta: Int): T

    abstract fun getPresentData(): T

    open fun getPresentPosition(): Int = itemCount/2+1
}

abstract class BaseViewHistoryFragment<T>: Fragment() {
    private val TAG = BaseViewHistoryFragment::class.simpleName

    companion object {
        private const val KEY_CURRENT_PAGE_POSITION = "key.current_page_position"
    }

    private var _binding: ViewHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private var _adapter: ViewHistoryPageAdapter<T>? = null
    protected val adapter get() = _adapter!!

    protected abstract fun onCreatePageAdapter(): ViewHistoryPageAdapter<T>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.historyViewPager.adapter = null
        _adapter = null
        _binding = null
    }

    private fun getSavedCurrentPosition(): Int? {
        return navController.currentBackStackEntry?.savedStateHandle?.get(KEY_CURRENT_PAGE_POSITION)
    }

    private fun saveCurrentPosition(position: Int) {
        navController.currentBackStackEntry?.savedStateHandle?.set(KEY_CURRENT_PAGE_POSITION, position)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_container)
        _adapter = onCreatePageAdapter()
        binding.historyViewPager.adapter = adapter
        val currentItem = getSavedCurrentPosition() ?: adapter.getPresentPosition()
        binding.historyViewPager.currentItem = currentItem
        changePageLabel(getCurrentData())
        binding.historyViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val data = adapter.getData(position)
                changePageLabel(data)
                saveCurrentPosition(position)
            }
        })
        binding.btnGotoPresent.text = getGotoPresentButtonText()
        binding.btnGotoPresent.setOnClickListener { setCurrentData(adapter.getPresentData()) }
        binding.btnDataPicker.setOnClickListener{ onClickDataPicker(getCurrentData()!!) }
        binding.addHistory.setOnClickListener {
            val target = binding.buttonsLayout
            if (target.isVisible()) {
                target.visibilityGone()
            }
            else {
                target.visible()
            }
        }
        binding.btnAddCredit.setOnClickListener { handleCreateHistory(HistoryType.CREDIT) }
        binding.btnAddDebit.setOnClickListener {  handleCreateHistory(HistoryType.DEBIT) }
//        binding.btnAddTransfer.setOnClickListener { handleCreateHistory(HistoryType.TRANSFER) }
    }

    abstract fun getGotoPresentButtonText(): CharSequence

    protected abstract fun onClickDataPicker(currentData: T)

    private fun handleCreateHistory(type: HistoryType) {
        val args = Bundle().apply {
            putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
            putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, type)
        }
        onPutCreateHistoryArgument(type, args)
        navController.navigate(R.id.action_history_list_to_history_input,args)
    }

    protected open fun onPutCreateHistoryArgument(type: HistoryType, argument: Bundle) {}

    protected open fun changePageLabel(data: T?) {
        binding.btnDataPicker.text = if (null == data) null else adapter.getDataLabel(data)
    }

    fun setCurrentData(data: T?) {
        data?.let {
            val position = adapter.getPositionForData(data)
            binding.historyViewPager.currentItem = position
        }
    }

    fun getCurrentData(): T? = adapter.getData(binding.historyViewPager.currentItem)
}