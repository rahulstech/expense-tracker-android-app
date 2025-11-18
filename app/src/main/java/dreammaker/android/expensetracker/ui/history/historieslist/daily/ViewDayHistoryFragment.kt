package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.databinding.HistoryListBinding
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryFilterData
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryListContainer
import dreammaker.android.expensetracker.ui.history.historieslist.HistorySummary
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryViewModel
import dreammaker.android.expensetracker.ui.main.ContextualActionBarViewModel
import dreammaker.android.expensetracker.util.AccountParcel
import dreammaker.android.expensetracker.util.GroupParcel
import dreammaker.android.expensetracker.util.SelectionHelper
import dreammaker.android.expensetracker.util.UIState
import dreammaker.android.expensetracker.util.getDate
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.History

class DayHistoryKeyProvider(private val adapter: DayHistoryListAdapter): ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long? = adapter.getSelectionKey(position)

    override fun getPosition(key: Long): Int = adapter.getKeyPosition(key)
}

class DayHistoryLookup(private val rv: RecyclerView): ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long?>? {
        val itemView = rv.findChildViewUnder(e.x,e.y)
        return itemView?.let { view ->
            val vh = rv.getChildViewHolder(view) as DayViewHolder
            vh.getSelectedItemDetails()
        }
    }
}

class ViewDayHistoryFragment : Fragment() {

    companion object {
        private val TAG = ViewDayHistoryFragment::class.simpleName
        const val ARG_DATE = "arg.date"
    }

    private var _binding: HistoryListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewHistoryViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }

    private lateinit var adapter: DayHistoryListAdapter
    private var isFirstVisible = true

    private lateinit var selectionHelper: SelectionHelper<Long>
    private val cabVm: ContextualActionBarViewModel by activityViewModels()

    private val cabMenu = object: MenuProvider {
        override fun onCreateMenu(
            menu: Menu,
            menuInflater: MenuInflater
        ) {
            menuInflater.inflate(R.menu.histories_list_cab_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when(menuItem.itemId) {
                R.id.delete_multiple -> {
                    onClickDeleteMultiple()
                    true
                }
                else -> false
            }
        }
    }

    private fun onClickDeleteMultiple() {
        if (selectionHelper.count() > 0) {
            QuickMessages.alertWarning(
                requireContext(),
                getString(R.string.message_warning_delete_multiple, selectionHelper.count()),
                QuickMessages.AlertButton(getString(R.string.label_no)),
                QuickMessages.AlertButton(getString(R.string.label_yes)) {
                    val ids = selectionHelper.getSelections()
                    viewModel.deleteHistories(ids)
                    cabVm.endContextActionBar()
                },
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HistoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = DayHistoryListAdapter()
        binding.historyList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.historyList.adapter = adapter


        selectionHelper = SelectionHelper<Long>(adapter) {
            SelectionTracker.Builder<Long>(
                "multipleDayHistorySelection",
                binding.historyList,
                DayHistoryKeyProvider(adapter),
                DayHistoryLookup(binding.historyList),
                StorageStrategy.createLongStorage()
            )
        }

        adapter.itemLongClickListener = { _,_,position ->
            selectionHelper.startSelection(SelectionPredicates.createSelectAnything()) { tracker ->
                cabVm.startContextualActionBar(cabMenu)

                tracker.addObserver(object: SelectionTracker.SelectionObserver<Long>(){
                    override fun onItemStateChanged(key: Long, selected: Boolean) {
                        cabVm.updateTitle(selectionHelper.count().toString())
                    }
                })

                selectionHelper.selectItem(adapter.getSelectionKey(position))
            }
        }

        selectionHelper.itemClickListener = { _,_,position -> handleItemClick(position) }

        viewLifecycleOwner.lifecycleScope.launch {
            cabVm.cabStartState.collectLatest { started ->
                if (!started) {
                    selectionHelper.endSelection()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteHistoriesState.collectLatest { state ->
                when (state) {
                    is UIState.UISuccess -> {
                        QuickMessages.toastSuccess(requireContext(),getString(R.string.message_success_delete_multiple_histories))
                    }
                    is UIState.UIError -> {
                        QuickMessages.simpleAlertError(requireContext(),R.string.message_fail_delete_multiple_histories)
                    }
                    else -> {}
                }
            }
        }

        binding.filterContainer.setOnCheckedStateChangeListener { _, _ -> filter() }

        observe()
    }

    override fun onPause() {
        super.onPause()
        cabVm.endContextActionBar()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstVisible) {
            loadHistories()
            isFirstVisible = false
        }
    }

    private fun observe() {
        viewModel.historySummary.observe(viewLifecycleOwner,this::onHistorySummaryPrepared)
        viewModel.getStateLiveData().observe(viewLifecycleOwner) { state ->
            when(state) {
                is UIState.UILoading -> {
                    binding.mainContainer.visibilityGone()
                    binding.shimmerContainer.startShimmer()
                    binding.shimmerContainer.visible()
                }
                is UIState.UISuccess -> {
                    onHistoryPrepared(state.data as List<History>)
                    binding.shimmerContainer.visibilityGone()
                    binding.mainContainer.visible()
                    binding.shimmerContainer.stopShimmer()
                }
                is UIState.UIError -> {
                    Log.e(TAG,"", state.cause)
                    // TODO: handle ui state error
                }
            }
        }
    }

    private fun loadHistories() {
        val date = requireArguments().getDate(ARG_DATE)!!
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        val entity = savedStateHandle?.get<Parcelable>(HistoryListContainer.ARG_SHOW_HISTORY_FOR)
        val params = ViewHistoryViewModel.HistoryLoadParams.forDate(date).apply {
            when(entity) {
                is AccountParcel -> ofAccount(entity.id)
                is GroupParcel -> ofGroup(entity.id)
            }
        }
        viewModel.loadHistories(params)
    }

    private fun onHistoryPrepared(histories: List<History>) {
        adapter.submitList(histories)
        if (histories.isEmpty()) {
            binding.historyList.visibilityGone()
            binding.emptyView.visible()
        }
        else {
            binding.emptyView.visibilityGone()
            binding.historyList.visible()
        }
        binding.shimmerContainer.visibilityGone()
        binding.mainContainer.visible()
        binding.shimmerContainer.stopShimmer()
    }

    private fun onHistorySummaryPrepared(summery: HistorySummary) {
        binding.totalCredit.text = summery.getTotalCreditText(requireContext())
        binding.totalDebit.text = summery.getTotalDebitText(requireContext())
    }

    private fun handleItemClick(position: Int) {
        val history = this.adapter.currentList[position]
        navController.navigate(R.id.action_history_list_to_view_history, Bundle().apply {
            putLong(Constants.ARG_ID, history.id)
        })
    }

    private fun filter() {
        val filterData = HistoryFilterData().apply {
            // TODO: handle filter
//            setTypes(getCheckedHistoryTypes())
        }
        viewModel.applyHistoryFilter(filterData)
    }

//    private fun getCheckedHistoryTypes(): Array<HistoryType> {
//        val types = arrayListOf<HistoryType>()
//        if (binding.filterCredit.isChecked) {
//            types.add(HistoryType.CREDIT)
//        }
//        if (binding.filterDebit.isChecked) {
//            types.add(HistoryType.DEBIT)
//        }
//        if (binding.filterTransfer.isChecked) {
//            types.add(HistoryType.TRANSFER)
//        }
//        return types.toTypedArray()
//    }
}