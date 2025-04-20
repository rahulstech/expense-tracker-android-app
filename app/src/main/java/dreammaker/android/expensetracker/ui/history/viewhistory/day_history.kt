package dreammaker.android.expensetracker.ui.history.viewhistory

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.DayHistoryListItemBinding
import dreammaker.android.expensetracker.databinding.HistoryListBinding
import dreammaker.android.expensetracker.ui.history.historyitem.ViewHistoryItemFragment
import dreammaker.android.expensetracker.ui.util.BaseSelectableItemListAdapter
import dreammaker.android.expensetracker.ui.util.ClickableViewHolder
import dreammaker.android.expensetracker.ui.util.getDate
import dreammaker.android.expensetracker.ui.util.putHistoryType

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
            binding.source.text = null
            binding.destination.text = null
            binding.type.text = null
            ViewCompat.setBackgroundTintList(binding.type, null)
        }
        else {
            binding.amount.text = history.amount?.toString()
            binding.note.text = history.note
            val type = history.type
            when (type) {
                HistoryType.TRANSFER -> {
                    setType(R.string.label_history_type_transfer, R.color.colorTransfer)
                    setSourceText(R.string.history_item_transfer_source, history.srcAccount?.name)
                    setDestinationText(R.string.history_item_transfer_destination, history.destAccount?.name)
                }
                HistoryType.CREDIT -> {
                    setType(R.string.label_history_type_credit, R.color.colorCredit)
                    setSourceText(R.string.history_item_credit_source, history.srcPerson?.name)
                    setDestinationText(R.string.history_item_credit_destination, history.destAccount?.name)
                }
                HistoryType.DEBIT -> {
                    setType(R.string.label_history_type_debit, R.color.colorDebit)
                    setSourceText(R.string.history_item_debit_source, history.srcAccount?.name)
                    setDestinationText(R.string.history_item_debit_destination, history.destPerson?.name)
                }
                else -> {}
            }
        }
    }

    private fun setSourceText(@StringRes resId: Int, text: CharSequence?) {
        binding.source.text = getString(resId, text ?: "")
    }

    private fun setDestinationText(@StringRes resId: Int, text: CharSequence?) {
        binding.destination.text = getString(resId, text ?: "")
    }

    private fun setType(@StringRes text: Int, @ColorRes backgroundTint: Int) {
        binding.type.text = getString(text)
        ViewCompat.setBackgroundTintList(binding.type, itemView.context.resources.getColorStateList(backgroundTint, null))
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