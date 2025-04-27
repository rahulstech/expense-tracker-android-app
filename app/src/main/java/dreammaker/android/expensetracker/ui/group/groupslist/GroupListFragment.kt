package dreammaker.android.expensetracker.ui.group.groupslist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.databinding.GroupsListBinding
import dreammaker.android.expensetracker.ui.person.personlist.GroupListViewModel
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible

class GroupListFragment : Fragment() {

    private val TAG = GroupListFragment::class.simpleName

    private var _binding:GroupsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GroupListViewModel
    private lateinit var adapter: GroupsListAdapter
    private lateinit var navController: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[GroupListViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GroupsListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        binding.list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = GroupsListAdapter()
        adapter.itemClickListener = { _,_,position -> handleClickGroup(adapter.currentList[position]) }
        binding.list.adapter = adapter
        binding.add.setOnClickListener {
            navController.navigate(R.id.action_groups_list_to_create_group, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
            })
        }
        viewModel.getAllGroups().observe(viewLifecycleOwner, this::onGroupsLoaded)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.person_list_menu, menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}