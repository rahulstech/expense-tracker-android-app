package dreammaker.android.expensetracker.ui.history.historieslist

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.ViewHistoryBinding
import dreammaker.android.expensetracker.util.AccountModelParcel
import dreammaker.android.expensetracker.util.Constants
import dreammaker.android.expensetracker.util.GroupModelParcel

abstract class ViewHistoryPageAdapter<T>(fragmentManager: FragmentManager, lifecycle: Lifecycle)
    : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val TAG = ViewHistoryPageAdapter::class.simpleName

    private var arguments: Bundle? = null

    final override fun createFragment(position: Int): Fragment {
        val data = getData(position)
        val fragment = onCreateFragment(position, data, arguments)
        return fragment
    }

    abstract fun onCreateFragment(position: Int, data: T, arguments: Bundle?): Fragment

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

    open fun getPresentPosition(): Int = itemCount-1

    open fun getMaxData(): T = getData(itemCount-1)

    open fun getMinData(): T = getData(0)

    fun putArguments(arguments: Bundle) {
        this.arguments = arguments
    }
}

abstract class BaseViewHistoryFragment<T>: Fragment() {
    private val TAG = BaseViewHistoryFragment::class.simpleName

    private var _binding: ViewHistoryBinding? = null
    private val binding get() = _binding!!
    private val navController: NavController by lazy { findNavController() }
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
        _adapter = null
        _binding = null
    }

    private fun getSavedCurrentPositionKey(): String = "${javaClass.simpleName}:current_page_position"

    private fun getSavedCurrentPosition(): Int? =  navController.currentBackStackEntry?.savedStateHandle?.get(getSavedCurrentPositionKey())

    private fun saveCurrentPosition(position: Int) {
        navController.currentBackStackEntry?.savedStateHandle?.set(getSavedCurrentPositionKey(), position)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _adapter = onCreatePageAdapter()
        binding.historyViewPager.adapter = adapter
        binding.historyViewPager.offscreenPageLimit = 1
        binding.historyViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) { handlePageChange(position) }
        })
        binding.btnGotoPresent.text = getGotoPresentButtonText()
        binding.btnGotoPresent.setOnClickListener { setCurrentData(adapter.getPresentData()) }
        binding.btnPicker.setOnClickListener{ onClickDataPicker(getCurrentData()!!) }
        binding.addHistory.setOnClickListener { navigateCreateHistory() }
    }

    override fun onResume() {
        super.onResume()
        val currentItem = getSavedCurrentPosition() ?: adapter.getPresentPosition()
        binding.historyViewPager.setCurrentItem(currentItem, false)
    }

    private fun handlePageChange(position: Int) {
        val data = adapter.getData(position)
        Log.d(TAG, "handlePageChange: position=$position data=$data")
        changePageLabel(data)
        saveCurrentPosition(position)
    }

    abstract fun getGotoPresentButtonText(): CharSequence

    protected abstract fun onClickDataPicker(currentData: T)

    private fun navigateCreateHistory() {
        val entity = navController.currentBackStackEntry?.savedStateHandle?.get<Parcelable>(HistoryListContainer.ARG_SHOW_HISTORY_FOR)
        val args = Bundle().apply {
            putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
            when (entity) {
                is AccountModelParcel -> putParcelable(Constants.ARG_ACCOUNT, entity)
                is GroupModelParcel -> putParcelable(Constants.ARG_GROUP, entity)
            }
        }
        onPutCreateHistoryArgument(args)
        navController.navigate(R.id.action_history_list_to_create_history,args)
    }

    protected open fun onPutCreateHistoryArgument(argument: Bundle) {}

    protected open fun changePageLabel(data: T?) {
        binding.btnPicker.text = if (null == data) null else adapter.getDataLabel(data)
    }

    fun setCurrentData(data: T?) {
        data?.let {
            val position = adapter.getPositionForData(data)
            binding.historyViewPager.currentItem = position
        }
    }

    fun getCurrentData(): T? = adapter.getData(binding.historyViewPager.currentItem)
}