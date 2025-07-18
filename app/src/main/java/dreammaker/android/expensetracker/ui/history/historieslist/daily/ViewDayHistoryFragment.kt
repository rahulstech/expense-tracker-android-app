package dreammaker.android.expensetracker.ui.history.historieslist.daily

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
import dreammaker.android.expensetracker.util.AccountModelParcel
import dreammaker.android.expensetracker.util.Constants
import dreammaker.android.expensetracker.util.GroupModelParcel
import dreammaker.android.expensetracker.util.UIState
import dreammaker.android.expensetracker.util.getDate
import dreammaker.android.expensetracker.util.putHistoryType
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible

class ViewDayHistoryFragment : Fragment() {

    companion object {
        private val TAG = ViewDayHistoryFragment::class.simpleName
        const val ARG_DATE = "arg.date"
    }

    private var _binding: HistoryListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewHistoryViewModel by viewModels()
    private lateinit var adapter: DayHistoryListAdapter
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
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter = DayHistoryListAdapter().also { this@ViewDayHistoryFragment.adapter = it }
        }
        adapter.itemClickListener = { _,_,position -> handleItemClick(position) }
        binding.filterContainer.setOnCheckedStateChangeListener { _, _ -> filter() }

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
        viewModel.historySummary.observe(viewLifecycleOwner,this::onHistorySummaryPrepared)
        viewModel.getStateLiveData().observe(viewLifecycleOwner) { state ->
            when(state) {
                is UIState.UILoading -> {
                    binding.mainContainer.visibilityGone()
                    binding.shimmerContainer.startShimmer()
                    binding.shimmerContainer.visible()
                }
                is UIState.UIData -> {
                    onHistoryPrepared(state.data)
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
        val date = requireArguments().getDate(ARG_DATE)!!
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        val entity = savedStateHandle?.get<Parcelable>(HistoryListContainer.ARG_SHOW_HISTORY_FOR)
        val params = ViewHistoryViewModel.HistoryLoadParams.forDate(date).apply {
            when(entity) {
                is AccountModelParcel -> ofAccount(entity.id)
                is GroupModelParcel -> ofGroup(entity.id)
            }
        }
        viewModel.loadHistories(params)
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
        val types = arrayListOf<HistoryType>()
        if (binding.filterCredit.isChecked) {
            types.add(HistoryType.CREDIT)
        }
        if (binding.filterDebit.isChecked) {
            types.add(HistoryType.DEBIT)
        }
        if (binding.filterTransfer.isChecked) {
            types.add(HistoryType.TRANSFER)
        }
        return types.toTypedArray()
    }
}