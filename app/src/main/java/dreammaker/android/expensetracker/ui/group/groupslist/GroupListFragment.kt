package dreammaker.android.expensetracker.ui.group.groupslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.databinding.GroupsListBinding
import dreammaker.android.expensetracker.util.SelectionHelper
import dreammaker.android.expensetracker.ui.UIState
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Group

class GroupListFragment : Fragment() {

    private lateinit var binding:GroupsListBinding
    private val viewModel: GroupListViewModel by viewModels()
    private lateinit var adapter: GroupsListAdapter
    private val navController: NavController by lazy { findNavController() }
    private lateinit var selectionHelper: SelectionHelper<Long>
    private val cabMenuProvider: MenuProvider = object: MenuProvider {
        override fun onCreateMenu(
            menu: Menu,
            menuInflater: MenuInflater
        ) {
            menuInflater.inflate(R.menu.groups_list_cab_menu,menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when(menuItem.itemId) {
            R.id.delete_multiple -> {
                onClickDeleteMultiple()
                true
            }
            else -> false
        }
    }

    private fun onClickDeleteMultiple() {
        if (selectionHelper.count() > 0) {
            QuickMessages.alertWarning(
                requireContext(),
                getString(R.string.message_warning_delete_multiple, selectionHelper.count()),
                QuickMessages.AlertButton(getString(R.string.label_no)),
                QuickMessages.AlertButton(getString(R.string.label_yes)) {
                    viewModel.deleteGroups(selectionHelper.getSelections())
                    selectionHelper.endSelection()
                },
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GroupsListBinding.inflate(inflater,container,false)
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
        binding.add.setOnClickListener { handleClickAddAccount() }

        adapter = GroupsListAdapter()
        binding.list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.list.adapter = adapter

        prepareItemSelection()

        viewModel.getAllGroups().observe(viewLifecycleOwner, this::onGroupsLoaded)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteGroupsState.filterNotNull()
                .collectLatest { state ->
                    when(state) {
                        is UIState.UISuccess -> {
                            QuickMessages.toastSuccess(requireContext(),getString(R.string.message_success_delete_multiple_groups))
                        }
                        is UIState.UIError -> {
                            QuickMessages.simpleAlertError(requireContext(),R.string.message_fail_delete_multiple_groups)
                        }
                        else -> {}
                    }
                }
        }
    }

    private fun prepareItemSelection() {
        selectionHelper = SelectionHelper(binding.list, adapter,this,viewLifecycleOwner)
        selectionHelper.prepareContextualActionBar(requireActivity(),cabMenuProvider)

        adapter.itemLongClickListener = { _,_,position ->
            selectionHelper.startSelection(
                contextualActionBar = true,
                initialSelection = adapter.getItemId(position)
            )
            true
        }

        selectionHelper.itemClickListener = { _,_,position -> handleClickGroup(adapter.currentList[position]) }

        selectionHelper.itemSelectionChangeCallback = { _,_,_ ->
            selectionHelper.cabViewModel?.cabTitle = selectionHelper.count().toString()
        }
    }

    private fun handleClickAddAccount() {
        navController.navigate(R.id.action_groups_list_to_create_group, bundleOf(
            Constants.ARG_ACTION to Constants.ACTION_CREATE
        ))
    }

    private fun onGroupsLoaded(groups: List<Group>) {
        adapter.submitList(groups)
        if (groups.isEmpty()) {
            binding.list.visibilityGone()
            binding.emptyView.visible()
        }
        else {
            binding.emptyView.visibilityGone()
            binding.list.visible()
        }
    }

    private fun handleClickGroup(group: Group) {
        navController.navigate(R.id.action_groups_list_to_view_group, Bundle().apply {
            putLong(Constants.ARG_ID,group.id)
        })
    }
}