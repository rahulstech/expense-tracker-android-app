package dreammaker.android.expensetracker.ui.history.viewhistory

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.DailyHistoryListItemBinding
import dreammaker.android.expensetracker.databinding.HistoryListBinding
import dreammaker.android.expensetracker.util.boldText

class DailyHistoryViewHolder(private val binding: DailyHistoryListItemBinding) : HistoryViewHolder(binding.root) {

    override fun bind(history: HistoryModel?) {
        if (history == null) {
            binding.amount.text = null
            binding.note.text = null
            binding.type.text = null
            binding.source.text = null
            binding.destination.text = null
        }
        else {
            binding.amount.text = history.amount?.toString()
            binding.note.text = history.note
            val type = history.type
            binding.type.text = type?.name
            when (type) {
                HistoryType.TRANSFER -> {
                    val src = history.srcAccount?.name
                    val dest = history.destAccount?.name
                    binding.source.text = getString(R.string.history_item_transfer_source, boldText(src))
                    binding.destination.text = getString(R.string.history_item_transfer_destination, boldText(dest))
                }
                HistoryType.CREDIT -> {
                    val src = history.srcPerson?.name
                    val dest = history.destAccount?.name
                    binding.source.text = getString(R.string.history_item_credit_source, boldText(src))
                    binding.destination.text = getString(R.string.history_item_credit_destination, boldText(dest))
                }
                HistoryType.DEBIT -> {
                    val src = history.srcAccount?.name
                    val dest = history.destPerson?.name
                    binding.source.text = getString(R.string.history_item_debit_source, boldText(src))
                    binding.destination.text = getString(R.string.history_item_debit_destination, boldText(dest))
                }

                HistoryType.EXPENSE -> TODO()
                HistoryType.INCOME -> TODO()
                null -> TODO()
            }
        }
    }
}

class DailyHistoryListAdapter: HistoryListAdapter<DailyHistoryViewHolder>() {

    override fun onCreateHistoryViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): DailyHistoryViewHolder
    = DailyHistoryViewHolder(
        DailyHistoryListItemBinding.inflate(inflater,parent,false)
    )
}

class ViewDayHistoryFragment(val date: Date) : Fragment() {

    private val TAG = ViewDayHistoryFragment::class.simpleName

    private lateinit var binding: HistoryListBinding

    private lateinit var viewModel: ViewHistoryViewModel

    private lateinit var adapter: DailyHistoryListAdapter

//    private lateinit var navController: NavController

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
//        navController = Navigation.findNavController(view)
        binding.historyList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        adapter = DailyHistoryListAdapter()
        binding.historyList.adapter = adapter
        viewModel.getDailyHistories(date).observe(viewLifecycleOwner) {
            Log.i(TAG, "no of histories for date $date ${it?.size ?: 0}")
            adapter.submitList(it)
        }
//        binding.btn.setOnClickListener{ onClickCreateHistory() }
    }

    private fun onClickCreateHistory() {
//        navController.navigate(R.id.action_history_list_to_history_input)
    }
}