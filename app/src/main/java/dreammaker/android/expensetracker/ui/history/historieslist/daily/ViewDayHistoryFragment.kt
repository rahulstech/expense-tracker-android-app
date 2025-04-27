package dreammaker.android.expensetracker.ui.history.historieslist.daily

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.databinding.HistoryListBinding
import dreammaker.android.expensetracker.ui.history.viewhistory.ViewHistoryItemFragment
import dreammaker.android.expensetracker.ui.history.historieslist.ViewHistoryViewModel
import dreammaker.android.expensetracker.ui.util.getDate
import dreammaker.android.expensetracker.ui.util.putHistoryType

class ViewDayHistoryFragment : Fragment() {

    companion object {
        private val TAG = ViewDayHistoryFragment::class.simpleName
        const val ARG_DATE = "arg.date"
    }

    private lateinit var binding: HistoryListBinding

    private lateinit var viewModel: ViewHistoryViewModel

    private lateinit var adapter: DayHistoryListAdapter

    private var navController: NavController? = null

    lateinit var date: Date

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

        date = requireArguments().getDate(ARG_DATE)!!
        viewModel.getDailyHistories(date).observe(viewLifecycleOwner, this::onHistoryLoaded)
    }

    private fun onHistoryLoaded(histories: List<HistoryModel>?) {
        adapter.submitList(histories)
        toggleEmptyViewAndHistoryListVisibility(histories?.isEmpty() == true)
    }

    private fun toggleEmptyViewAndHistoryListVisibility(showHistoryList: Boolean) {
        if (showHistoryList) {
            binding.historyList.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        }
        else {
            binding.emptyView.visibility = View.GONE
            binding.historyList.visibility = View.VISIBLE
        }
    }

    private fun handleItemClick(adapter: RecyclerView.Adapter<*>, view: View, position: Int) {
        val history = this.adapter.currentList[position]
        var args = Bundle().apply {
            putLong(ViewHistoryItemFragment.ARG_HISTORY_ID, history.id!!)
            putHistoryType(ViewHistoryItemFragment.ARG_HISTORY_TYPE, history.type!!)
        }
        navController?.navigate(R.id.action_history_list_to_history_item, args)
    }
}