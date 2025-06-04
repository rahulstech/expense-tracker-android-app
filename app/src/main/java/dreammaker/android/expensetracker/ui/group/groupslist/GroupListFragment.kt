package dreammaker.android.expensetracker.ui.group.groupslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.databinding.GroupsListBinding
import dreammaker.android.expensetracker.ui.person.personlist.GroupListViewModel
import dreammaker.android.expensetracker.util.Constants
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible

class GroupListFragment : Fragment() {

    private val TAG = GroupListFragment::class.simpleName

    private var _binding:GroupsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroupListViewModel by viewModels()
    private lateinit var adapter: GroupsListAdapter
    private val navController: NavController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GroupsListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.list.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = GroupsListAdapter().also {
                this@GroupListFragment.adapter = it
            }
        }
        adapter.itemClickListener = { _,_,position -> handleClickGroup(adapter.currentList[position]) }
        binding.add.setOnClickListener { handleClickAddAccount() }
        viewModel.getAllGroups().observe(viewLifecycleOwner, this::onGroupsLoaded)
    }

    private fun handleClickAddAccount() {
        navController.navigate(R.id.action_groups_list_to_create_group, Bundle().apply {
            putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
        })
    }

    private fun onGroupsLoaded(groups: List<GroupModel>) {
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

    private fun handleClickGroup(group: GroupModel) {
        navController.navigate(R.id.action_groups_list_to_view_group, Bundle().apply {
            putLong(Constants.ARG_ID,group.id!!)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}