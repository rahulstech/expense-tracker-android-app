package dreammaker.android.expensetracker.ui.history.historieslist.monthly

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.HistoryListBinding
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryFilterData
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryListContainer
import dreammaker.android.expensetracker.ui.history.historieslist.HistorySummary
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryViewModel
import dreammaker.android.expensetracker.ui.history.viewhistory.ViewHistoryItemFragment
import dreammaker.android.expensetracker.ui.util.AccountModelParcel
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.GroupModelParcel
import dreammaker.android.expensetracker.ui.util.getMonthYear
import dreammaker.android.expensetracker.ui.util.putHistoryType
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible

class ViewMonthHistoryFragment : Fragment() {

    companion object {
        private val TAG = ViewMonthHistoryFragment::class.simpleName
        const val ARG_MONTH_YEAR = "arg.month_year"
    }

    private lateinit var binding: HistoryListBinding

    private val viewModel: ViewHistoryViewModel by viewModels()

    private lateinit var adapter: MonthHistoryListAdapter

    private val navController: NavController by lazy { findNavController() }

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
        adapter.itemClickListener = { _,_,position -> handleItemClick(position) }
        binding.filterContainer.setOnCheckedStateChangeListener { _,_ -> filter() }

        observeSummary()
        observeHistory()
    }

    private fun observeSummary() {
        viewModel.historySummary.observe(viewLifecycleOwner, this::onHistorySummaryPrepared)
    }

    private fun observeHistory() {
        val monthYear = requireArguments().getMonthYear(ARG_MONTH_YEAR)!!
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        val entity = savedStateHandle?.get<Parcelable?>(HistoryListContainer.ARG_SHOW_HISTORY_FOR)
        val histories = when(entity) {
            is AccountModelParcel -> viewModel.getMonthlyHistoriesForAccount(monthYear, entity.id)
            is GroupModelParcel -> viewModel.getMonthlyHistoriesForGroup(monthYear,entity.id)
            else -> viewModel.getMonthlyHistories(monthYear)
        }
        histories.observe(viewLifecycleOwner, this::onHistoryPrepared)
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

    private fun handleItemClick(position: Int) {
        val history = this.adapter.currentList[position]
        navController.navigate(R.id.action_history_list_to_view_history, Bundle().apply {
            putLong(Constants.ARG_ID, history.id!!)
            putHistoryType(ViewHistoryItemFragment.ARG_HISTORY_TYPE, history.type!!)
        })
    }

    private fun filter() {
        val filterData = HistoryFilterData().apply {
            setTypes(getCheckedHistoryTypes())
        }
        viewModel.applyHistoryFilter(filterData)
    }

    private fun getCheckedHistoryTypes(): Array<HistoryType> {
        var types = arrayOf<HistoryType>()
        if (binding.filterCredit.isChecked) {
            types = types.plusElement(HistoryType.CREDIT)
        }
        if (binding.filterDebit.isChecked) {
            types = types.plusElement(HistoryType.DEBIT)
        }
        if (binding.filterTransfer.isChecked) {
            types = types.plusElement(HistoryType.DEBIT)
        }
        return types
    }
}