package dreammaker.android.expensetracker.ui.history.viewhistory

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.DayHistoryListItemBinding
import dreammaker.android.expensetracker.databinding.HistoryListBinding
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.ui.util.ClickableViewHolder
import dreammaker.android.expensetracker.ui.util.getDate
import dreammaker.android.expensetracker.util.boldText

class DayHistoryViewHolder(
    private val binding: DayHistoryListItemBinding,
    onClick: (DayHistoryViewHolder, View)->Unit
) :  ClickableViewHolder<DayHistoryViewHolder>(binding.root, onClick) {

    init {
        attachItemClickListener()
    }

    fun bind(history: HistoryModel?, selected: Boolean) {
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

private val callback = object: DiffUtil.ItemCallback<HistoryModel>() {
    override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean =
        oldItem.equals(newItem)
}

class DayHistoryListAdapter: BaseSelectableItemListAdapter<HistoryModel, Long, DayHistoryViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DayHistoryListItemBinding.inflate(inflater,parent,false)
        return DayHistoryViewHolder(binding, this::handleItemClick)
    }
    override fun onBindViewHolder(holder: DayHistoryViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, isSelected(position))
    }

    override fun getItemId(position: Int): Long = getItem(position)?.id ?: RecyclerView.NO_ID

    override fun getSelectionKey(position: Int): Long = getItemId(position)
}

class ViewDayHistoryFragment : Fragment() {

    companion object {
        private val TAG = ViewDayHistoryFragment::class.simpleName
        const val ARG_DATE = "arg.date"
    }

    private lateinit var binding: HistoryListBinding

    private lateinit var viewModel: ViewHistoryViewModel

    private lateinit var adapter: DayHistoryListAdapter

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
        binding.historyList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        adapter = DayHistoryListAdapter()
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
}