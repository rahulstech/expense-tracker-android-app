package dreammaker.android.expensetracker.ui.history.historieslist

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
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.databinding.FragmentHistoryListBinding
import dreammaker.android.expensetracker.settings.SettingsProvider
import dreammaker.android.expensetracker.settings.ViewHistory
import dreammaker.android.expensetracker.ui.HistoryListItem
import dreammaker.android.expensetracker.util.AccountParcelable
import dreammaker.android.expensetracker.util.GroupParcelable
import dreammaker.android.expensetracker.util.SelectionHelper
import dreammaker.android.expensetracker.util.setActivitySubTitle
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible

class HistoryKeyProvider(private val adapter: HistoryListAdapter): ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long? = adapter.getSelectionKey(position)

    override fun getPosition(key: Long): Int = adapter.getKeyPosition(key)
}

class HistoryLookup(private val rv: RecyclerView): ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long?>? {
        val itemView = rv.findChildViewUnder(e.x,e.y)
        return itemView?.let { view ->
            val vh = rv.getChildViewHolder(view) as HistoryViewHolder
            vh.getSelectedItemDetails()
        }
    }
}

class HistorySelectionPredicate(val adapter: HistoryListAdapter): SelectionTracker.SelectionPredicate<Long>() {

    override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean = key != RecyclerView.NO_ID

    override fun canSetStateAtPosition(
        position: Int,
        nextState: Boolean
    ): Boolean = adapter.getItemViewType(position) == HistoryListAdapter.TYPE_ITEM

    override fun canSelectMultiple(): Boolean = true
}

class HistoryListFragment: Fragment(), MenuProvider {

    companion object {
        private val TAG = HistoryListFragment::class.simpleName
    }

    private val viewModel: ViewHistoryViewModel by viewModels()
    private val navController by lazy { findNavController() }

    private lateinit var binding: FragmentHistoryListBinding

    private val datePicker by lazy {
        DayPicker(requireContext()).apply {
            pickerCallback = { picker,item ->
               updatePickerText()
                loadHistories()
            }
        }
    }

    private val monthPicker by lazy {
        MonthPicker(requireContext()).apply {
            pickerCallback = { picker,item ->
                updatePickerText()
                loadHistories()
            }
        }
    }

    private val settings by lazy { SettingsProvider.get(requireContext()) }
    private lateinit var picker: DateRangePicker
    private lateinit var adapter: HistoryListAdapter
    private lateinit var selectionHelper: SelectionHelper<Long>
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
                    viewModel.deleteHistories(selectionHelper.getSelections())
                    selectionHelper.endSelection()
                },
            )
        }
    }

    private fun getArgHistoriesOf(): Parcelable? = BundleCompat.getParcelable(
        requireArguments(),
        Constants.ARG_HISTORIES_OF,
        Parcelable::class.java
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnPicker.setOnClickListener {
            picker.pick()
        }
        binding.btnGotoPresent.setOnClickListener {
            picker.selectDefault()
        }
        binding.addHistory.setOnClickListener {
            navigateCreateHistory()
        }

        adapter = HistoryListAdapter()
        binding.historyList.adapter = adapter

        prepareItemSelection()
        observeLoadingState()
        observeHistoryLoading()

        changeViewHistory(settings.getViewHistory())
        picker.restoreState(savedInstanceState)
        updatePickerText()

        (requireActivity() as MenuHost).addMenuProvider(this,viewLifecycleOwner)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.history_list_menu,menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        val viewAs = settings.getViewHistory()
        when(viewAs) {
            ViewHistory.MONTHLY -> menu.findItem(R.id.menu_view_as_monthly).isChecked = true
            ViewHistory.DAILY -> menu.findItem(R.id.menu_view_as_daily).isChecked = true
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
        when(menuItem.itemId) {
            R.id.menu_view_as_daily -> {
                changeViewHistory(ViewHistory.DAILY)
                true
            }
            R.id.menu_view_as_monthly -> {
                changeViewHistory(ViewHistory.MONTHLY)
                true
            }
            else -> false
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        picker.saveState(outState)
    }

    override fun onResume() {
        super.onResume()
            getArgHistoriesOf()?.let { entity ->
            when(entity) {
                is AccountParcelable -> setActivitySubTitle(entity.name)
                is GroupParcelable -> setActivitySubTitle(entity.name)
                else -> setActivitySubTitle(null)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        setActivitySubTitle(null)
    }

    private fun changeViewHistory(viewAs: ViewHistory) {
        settings.setViewHistory(viewAs)
        requireActivity().invalidateOptionsMenu()
        changePicker(viewAs)
        loadHistories()
    }

    private fun changePicker(viewAs: ViewHistory) {
        picker = when(viewAs) {
            ViewHistory.DAILY -> datePicker
            ViewHistory.MONTHLY -> monthPicker
        }
        binding.btnGotoPresent.text = picker.getDefaultLabel()
        updatePickerText()
    }

    private fun updatePickerText() {
        binding.btnPicker.text = picker.getSelectionLabel()
    }

    private fun loadHistories() {
        val otherThanDailyView = settings.getViewHistory() != ViewHistory.DAILY
        val dateRange = picker.getSelection()
        val entity = getArgHistoriesOf()
        val params = when(entity) {
            is AccountParcelable -> LoadHistoryParameters.ofAccount(entity.id)
            is GroupParcelable -> LoadHistoryParameters.ofGroup(entity.id)
            else -> {
                return
            }
        }
        params.apply {
            betweenDates(dateRange.first, dateRange.second)
            withHeaders(otherThanDailyView)
        }
        viewModel.loadHistories(params)
    }

    private fun observeLoadingState() {
        adapter.addLoadStateListener { loadStates ->

            // initial loading start
            if (loadStates.refresh is LoadState.Loading) {
                binding.apply {
                    toggleShimmer(true)
                }
                return@addLoadStateListener
            }

            // initial loading complete
            if (loadStates.refresh is LoadState.NotLoading) {
                binding.apply {
                    toggleShimmer(false)
                    toggleEmptyView(adapter.itemCount==0)
                }
                return@addLoadStateListener
            }
        }
    }

    private fun observeHistoryLoading() {
        viewModel.histories.observe(viewLifecycleOwner){
            Log.i(TAG,"history loading complete")
            Log.d(TAG, "pagingData = $it")
            adapter.submitData(lifecycle,it)
            toggleEmptyView(adapter.itemCount==0)
        }
    }

    private fun toggleShimmer(showShimmer: Boolean) {
        binding.apply {
            if (showShimmer) {
                historyList.visibilityGone()
                emptyView.visibilityGone()
                shimmerContainer.startShimmer()
                shimmerContainer.visible()
            }
            else {
                shimmerContainer.visibilityGone()
                shimmerContainer.stopShimmer()
                emptyView.visible()
            }
        }
    }

    private fun toggleEmptyView(showEmptyView: Boolean) {
        binding.apply {
            if (showEmptyView) {
                historyList.visibilityGone()
                emptyView.visible()
            }
            else {
                emptyView.visibilityGone()
                historyList.visible()
            }
        }
    }

    private fun navigateCreateHistory() {
        val entity = getArgHistoriesOf()
        Log.d(TAG,"navigateCreateHistory: entity = $entity")
        val args = Bundle().apply {
            putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
            when(entity) {
                is AccountParcelable -> putParcelable(Constants.ARG_ACCOUNT, entity)
                is GroupParcelable -> putParcelable(Constants.ARG_GROUP,entity)
            }
        }
        navController.navigate(R.id.action_history_list_to_create_history,args)
    }

    private fun prepareItemSelection() {
        selectionHelper = SelectionHelper(adapter,this,viewLifecycleOwner) {
            SelectionTracker.Builder(
                "multipleDayHistorySelection",
                binding.historyList,
                HistoryKeyProvider(adapter),
                HistoryLookup(binding.historyList),
                StorageStrategy.createLongStorage()
            ).withSelectionPredicate(
                HistorySelectionPredicate(adapter)
            )
        }.apply {
            prepareContextualActionBar(requireActivity(),cabMenu)
        }

        adapter.itemLongClickListener = { _,_,position ->
            selectionHelper.startSelection(true, adapter.getSelectionKey(position))
            true
        }

        selectionHelper.itemClickListener = { _,_,position -> handleItemClick(position) }

        selectionHelper.itemSelectionChangeCallback = { _,_,_,_ ->
            selectionHelper.cabViewModel?.cabTitle = selectionHelper.count().toString()
        }
    }

    private fun handleItemClick(position: Int) {
        val item = adapter.peek(position)
        if (item is HistoryListItem.Item) {
            navController.navigate(R.id.action_history_list_to_view_history, bundleOf(
                Constants.ARG_ID to item.history.id
            ))
        }
    }
}