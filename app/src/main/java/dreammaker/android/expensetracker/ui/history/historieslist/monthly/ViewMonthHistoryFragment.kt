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
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.HistoryListBinding
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryFilterData
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryListContainer
import dreammaker.android.expensetracker.ui.history.historieslist.HistorySummary
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryViewModel
import dreammaker.android.expensetracker.util.AccountParcel
import dreammaker.android.expensetracker.util.GroupParcel
import dreammaker.android.expensetracker.util.UIState
import dreammaker.android.expensetracker.util.getMonthYear
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import rahulstech.android.expensetracker.domain.model.History

class ViewMonthHistoryFragment : Fragment() {

    companion object {
        private val TAG = ViewMonthHistoryFragment::class.simpleName
        const val ARG_MONTH_YEAR = "arg.month_year"
    }

    private var _binding: HistoryListBinding? = null
    private val binding: HistoryListBinding get() = _binding!!
    private val viewModel: ViewHistoryViewModel by viewModels()
    private lateinit var adapter: MonthHistoryListAdapter
    private val navController: NavController by lazy { findNavController() }
    private var isFirstVisible = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HistoryListBinding.inflate(inflater, container, false)
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

        observe()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstVisible) {
            loadHistories()
            isFirstVisible = false
        }
    }

    private fun observe() {
        viewModel.historySummary.observe(viewLifecycleOwner, this::onHistorySummaryPrepared)
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
                    // TODO: handle ui state error
                }
            }
        }
    }

    private fun loadHistories() {
        val monthYear = requireArguments().getMonthYear(ARG_MONTH_YEAR)!!
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        val entity = savedStateHandle?.get<Parcelable?>(HistoryListContainer.ARG_SHOW_HISTORY_FOR)
        val params = ViewHistoryViewModel.HistoryLoadParams.forMonthYear(monthYear).apply {
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
        // TODO: apply filter
        val filterData = HistoryFilterData().apply {
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
//           types.add(HistoryType.TRANSFER)
//        }
//        return types.toTypedArray()
//    }
}