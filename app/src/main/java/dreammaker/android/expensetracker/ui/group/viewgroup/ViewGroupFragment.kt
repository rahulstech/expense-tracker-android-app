package dreammaker.android.expensetracker.ui.group.viewgroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.databinding.ViewGroupLayoutBinding
import dreammaker.android.expensetracker.util.GroupParcelable
import dreammaker.android.expensetracker.ui.UIState
import dreammaker.android.expensetracker.util.getDueLabel
import dreammaker.android.expensetracker.util.getBalanceText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Group

class ViewGroupFragment: Fragment(), MenuProvider {

    private val TAG = ViewGroupFragment::class.simpleName

    private var _binding: ViewGroupLayoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewGroupViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }

    private fun getArgGroupId(): Long = requireArguments().getLong(Constants.ARG_ID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewGroupLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnViewHistory.setOnClickListener { handleClickViewHistory() }
        binding.addHistory.setOnClickListener { navigateToCreateHistory() }
        viewModel.findGroupById(getArgGroupId()).observe(viewLifecycleOwner, this::onGroupLoaded)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteGroupState.collectLatest { state ->
                when(state) {
                    is UIState.UISuccess -> {
                        QuickMessages.toastSuccess(requireContext(),getString(R.string.message_success_delete_group))
                        navController.popBackStack()
                    }
                    is UIState.UIError -> {
                        QuickMessages.simpleAlertError(requireContext(),R.string.message_fail_delete_group)
                    }
                    else -> {}
                }
            }
        }
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner)
    }

    private fun onGroupLoaded(group: Group?) {
        if (null == group) {
            QuickMessages.toastError(requireContext(),getString(R.string.message_group_not_found))
            navController.popBackStack()
        }
        else {
            binding.name.text = group.name
            binding.due.text = group.getBalanceText(requireContext())
            binding.balanceLabel.text = group.getDueLabel(requireContext())
            requireActivity().invalidateOptionsMenu()
        }
    }

    private fun handleClickViewHistory() {
        val group = viewModel.getStoredGroup()
        group?.let {
            navController.navigate(R.id.action_view_group_to_history_list, Bundle().apply {
                putParcelable(Constants.ARG_HISTORIES_OF, GroupParcelable(group))
            })
        }
    }

    private fun navigateToCreateHistory() {
        val group = viewModel.getStoredGroup()
        group?.let {
            navController.navigate(R.id.action_view_group_to_add_history,Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
                putParcelable(Constants.ARG_GROUP, GroupParcelable(group))
            })
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        viewModel.getStoredGroup()?.let {
            inflater.inflate(R.menu.view_group_menu, menu)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                onClickDelete()
                true
            }
            R.id.edit -> {
                onClickEdit()
                true
            }
            else -> false
        }
    }

    private fun onClickEdit() {
        navController.navigate(R.id.action_view_group_to_edit_group, Bundle().apply {
            putString(Constants.ARG_ACTION, Constants.ACTION_EDIT)
            putLong(Constants.ARG_ID, getArgGroupId())
        })
    }

    private fun onClickDelete() {
        val group = viewModel.getStoredGroup()
        group?.let {
            QuickMessages.alertWarning(requireContext(),
                getString(R.string.message_warning_delete,group.name),
                QuickMessages.AlertButton(getString(R.string.label_no)),
                QuickMessages.AlertButton(getString(R.string.label_yes)){
                    viewModel.removeGroup(group)
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}