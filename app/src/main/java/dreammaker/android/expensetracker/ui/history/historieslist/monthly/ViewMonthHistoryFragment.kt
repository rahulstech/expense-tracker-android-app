package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.HistoryListBinding
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryFilterData
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryListContainer
import dreammaker.android.expensetracker.ui.history.historieslist.HistorySummary
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryViewModel
import dreammaker.android.expensetracker.ui.history.historieslist.doFilterHistory
import dreammaker.android.expensetracker.ui.history.viewhistory.ViewHistoryItemFragment
import dreammaker.android.expensetracker.ui.util.AccountModelParcel
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.Filter
import dreammaker.android.expensetracker.ui.util.GroupModelParcel
import dreammaker.android.expensetracker.ui.util.getMonthYear
import dreammaker.android.expensetracker.ui.util.putHistoryType
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible
import kotlinx.coroutines.launch

class ViewMonthHistoryFragment : Fragment() {

    companion object {
        private val TAG = ViewMonthHistoryFragment::class.simpleName
        const val ARG_MONTH_YEAR = "arg.month_year"
    }

    private lateinit var binding: HistoryListBinding

    private val viewModel: ViewHistoryViewModel by viewModels()

    private lateinit var adapter: MonthHistoryListAdapter

    private val navController: NavController by lazy { findNavController() }

    private val filter = Filter<HistoryFilterData,List<HistoryModel>>{ query,histories -> doFilterHistory(query,histories) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HistoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.historyList.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            adapter = MonthHistoryListAdapter().also {
                this@ViewMonthHistoryFragment.adapter = it
            }
        }
        adapter.itemClickListener = this::handleItemClick
        binding.filterContainer.setOnCheckedStateChangeListener { _,_ -> filter() }
        val monthYear = requireArguments().getMonthYear(ARG_MONTH_YEAR)!!
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        val entity = savedStateHandle?.get<Parcelable?>(HistoryListContainer.ARG_SHOW_HISTORY_FOR)
        val histories = when(entity) {
            is AccountModelParcel -> viewModel.getMonthlyHistoriesForAccount(monthYear, entity.id)
            is GroupModelParcel -> viewModel.getMonthlyHistoriesForGroup(monthYear,entity.id)
            else -> viewModel.getMonthlyHistories(monthYear)
        }

        lifecycleScope.launch { filter.start() }
        filter.resultLiveData.observe(viewLifecycleOwner, this::onHistoryPrepared)
        viewModel.historySummary.observe(viewLifecycleOwner, this::onHistorySummaryPrepared)
        histories.observe(viewLifecycleOwner) { onHistoryLoaded()}
    }

    private fun onHistoryPrepared(histories: List<HistoryModel>) {
        adapter.submitList(histories)
        if (histories.isEmpty()) {
            binding.historyList.visibilityGone()
            binding.emptyView.visible()
        }
        else {
            binding.emptyView.visibilityGone()
            binding.historyList.visible()
        }
    }

    private fun onHistorySummaryPrepared(summery: HistorySummary) {
        binding.totalCredit.text = summery.getTotalCreditText(requireContext())
        binding.totalDebit.text = summery.getTotalDebitText(requireContext())
    }

    private fun onHistoryLoaded() {
        obtainSummary()
        filter()
    }

    private fun handleItemClick(adapter: RecyclerView.Adapter<*>, view: View, position: Int) {
        val history = this.adapter.currentList[position]
        navController.navigate(R.id.action_history_list_to_view_history, Bundle().apply {
            putLong(Constants.ARG_ID, history.id!!)
            putHistoryType(ViewHistoryItemFragment.ARG_HISTORY_TYPE, history.type!!)
        })
    }

    private fun filter() {
        val histories = viewModel.getHistories()
        val query = HistoryFilterData().apply {
            setTypes(getCheckedHistoryTypes())
        }
        filter.filter(query,histories)
    }

    private fun obtainSummary() {
        val histories = viewModel.getHistories()
        viewModel.obtainSummary(histories)
    }

    private fun getCheckedHistoryTypes(): List<HistoryType> {
        val checkedIds = binding.filterContainer.checkedChipIds
        return checkedIds.mapNotNull { id ->
            when (id) {
                R.id.filter_credit -> HistoryType.CREDIT
                R.id.filter_debit -> HistoryType.DEBIT
                R.id.filter_transfer -> HistoryType.TRANSFER
                else -> null
            }
        }
    }
}