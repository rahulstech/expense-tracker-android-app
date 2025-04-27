package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.databinding.HistoryListBinding
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryViewModel
import dreammaker.android.expensetracker.ui.history.viewhistory.ViewHistoryItemFragment
import dreammaker.android.expensetracker.ui.util.AccountModelParcel
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.GroupModelParcel
import dreammaker.android.expensetracker.ui.util.getDate
import dreammaker.android.expensetracker.ui.util.putHistoryType
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible

class ViewDayHistoryFragment : Fragment() {

    companion object {
        private val TAG = ViewDayHistoryFragment::class.simpleName
        const val ARG_DATE = "arg.date"
    }

    private lateinit var binding: HistoryListBinding

    private lateinit var viewModel: ViewHistoryViewModel

    private lateinit var adapter: DayHistoryListAdapter

    private var navController: NavController? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[ViewHistoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HistoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_container)
        binding.historyList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        adapter = DayHistoryListAdapter()
        adapter.itemClickListener = this::handleItemClick
        binding.historyList.adapter = adapter

        val date = requireArguments().getDate(ARG_DATE)!!
        val account = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_ACCOUNT, AccountModelParcel::class.java) }
        val group = arguments?.let { BundleCompat.getParcelable(it, Constants.ARG_GROUP, GroupModelParcel::class.java) }
        val histories = if (null !=  account) {
            viewModel.getDailyHistoriesForAccount(date, account.id)
        } else if (null != group) {
            viewModel.getDailyHistoriesForGroup(date,group.id)
        } else {
            viewModel.getDailyHistories(date)
        }
        histories.observe(viewLifecycleOwner, this::onHistoryLoaded)
    }

    private fun onHistoryLoaded(histories: List<HistoryModel>?) {
        adapter.submitList(histories)
        if (histories.isNullOrEmpty()) {
            binding.historyList.visibilityGone()
            binding.emptyView.visible()
        }
        else {
            binding.emptyView.visibilityGone()
            binding.historyList.visible()
        }
    }

    private fun handleItemClick(adapter: RecyclerView.Adapter<*>, view: View, position: Int) {
        val history = this.adapter.currentList[position]
        navController?.navigate(R.id.action_history_list_to_view_history, Bundle().apply {
            putLong(ViewHistoryItemFragment.ARG_HISTORY_ID, history.id!!)
            putHistoryType(ViewHistoryItemFragment.ARG_HISTORY_TYPE, history.type!!)
        })
    }
}