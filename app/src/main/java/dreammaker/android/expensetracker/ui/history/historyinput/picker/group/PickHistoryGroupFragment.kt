package dreammaker.android.expensetracker.ui.history.historyinput.picker.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.SingleGroupPickerListWithSearchLayoutBinding
import dreammaker.android.expensetracker.ui.GroupListItem
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputViewModel
import dreammaker.android.expensetracker.util.SelectionHelper
import dreammaker.android.expensetracker.util.setActivityTitle
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import rahulstech.android.expensetracker.domain.model.Group

class PickHistoryGroupFragment : Fragment() {

    private lateinit var binding: SingleGroupPickerListWithSearchLayoutBinding
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
        binding = SingleGroupPickerListWithSearchLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.searchBar.searchInput.apply {
            hint = getString(R.string.label_search_group_name)
            setText(viewModel.searchText)
            addTextChangedListener { editable ->
                viewModel.searchText = editable.toString()
            }
        }
        binding.btnChoose.setOnClickListener { handlePickGroup() }

        adapter = GroupPickerListAdapter()
        binding.optionsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.optionsList.adapter = adapter

        prepareItemSelection()

        viewModel.getAllGroups().observe(viewLifecycleOwner, this::onGroupsLoaded)
    }

    override fun onResume() {
        super.onResume()
        setActivityTitle(R.string.title_choose_group)
    }

    private fun prepareItemSelection() {
        selectionHelper = SelectionHelper(binding.optionsList,adapter,this,viewLifecycleOwner)

        selectionHelper.startSelection(selectMultiple = false, initialSelection = getInitialSelection())
    }

    private fun getInitialSelection(): Long? {
        return historyViewModel.getGroup()?.id
    }

    private fun onGroupsLoaded(groups: List<GroupListItem>) {
        adapter.submitList(groups)
        if (groups.isEmpty()) {
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
        val key = selectionHelper.getSelection() ?: return null
        val item = viewModel.groupListItems.find { item ->
            item is GroupListItem.Item && item.data.id == key
        }
        return (item as? GroupListItem.Item)?.data
    }
}