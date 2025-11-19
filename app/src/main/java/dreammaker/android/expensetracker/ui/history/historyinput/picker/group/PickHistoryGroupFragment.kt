package dreammaker.android.expensetracker.ui.history.historyinput.picker.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dreammaker.android.expensetracker.databinding.SingleGroupPickerListWithSearchLayoutBinding
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputViewModel
import dreammaker.android.expensetracker.util.SelectionHelper
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import rahulstech.android.expensetracker.domain.model.Group

class GroupPickerSelectionKeyProvider(private val adapter: GroupPickerListAdapter): ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long? = adapter.getSelectionKey(position)

    override fun getPosition(key: Long): Int = adapter.getKeyPosition(key)
}

class GroupPickerDetailsLookup(private val recyclerView: RecyclerView): ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long?>? {
        val itemView = recyclerView.findChildViewUnder(e.x,e.y)
        return itemView?.let { child ->
            val vh = recyclerView.getChildViewHolder(child) as GroupPickerViewHolder
            vh.getSelectedItemDetails()
        }
    }
}

class PickHistoryGroupFragment : Fragment() {

    private var _binding: SingleGroupPickerListWithSearchLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GroupPickerListAdapter
    private val navController: NavController by lazy { findNavController() }
    private val viewModel: GroupPickerViewModel by viewModels()
    private val historyViewModel: HistoryInputViewModel by activityViewModels()
    private lateinit var selectionHelper: SelectionHelper<Long>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SingleGroupPickerListWithSearchLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnChoose.setOnClickListener { handlePickGroup() }

        adapter = GroupPickerListAdapter()
        binding.optionsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.optionsList.adapter = adapter

        prepareItemSelection()

        viewModel.getAllGroups().observe(viewLifecycleOwner, this::onGroupsLoaded)
    }

    private fun prepareItemSelection() {
        selectionHelper = SelectionHelper<Long>(adapter,this,viewLifecycleOwner) {
            SelectionTracker.Builder<Long>(
                "singleAccountSelection",
                binding.optionsList,
                GroupPickerSelectionKeyProvider(adapter),
                GroupPickerDetailsLookup(binding.optionsList),
                StorageStrategy.createLongStorage()
            ).withSelectionPredicate(
                SelectionPredicates.createSelectSingleAnything()
            )
        }

        selectionHelper.startSelection() {
            selectionHelper.selectItem(getInitialSelection())
        }
    }

    private fun getInitialSelection(): Long? {
        return historyViewModel.getGroup()?.id
    }

    private fun onGroupsLoaded(accounts: List<Group>) {
        adapter.submitList(accounts)
        if (accounts.isEmpty()) {
            binding.emptyPlaceholder.visibilityGone()
            binding.emptyPlaceholder.visible()
        }
        else {
            binding.emptyPlaceholder.visibilityGone()
            binding.optionsList.visible()
        }
    }

    private fun handlePickGroup() {
        val selectedGroup = getSelectedGroup()
        historyViewModel.setGroup(selectedGroup)
        navController.popBackStack()
    }

    private fun getSelectedGroup(): Group? {
        if (selectionHelper.count()==0) return null
        val key = selectionHelper.getSelections()[0]
        return viewModel.getAllGroups().value?.find { it.id == key }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}