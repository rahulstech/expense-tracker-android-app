package dreammaker.android.expensetracker.ui.group.viewgroup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.ViewGroupLayoutBinding
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.GroupModelParcel
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.isVisible
import dreammaker.android.expensetracker.ui.util.putHistoryType
import dreammaker.android.expensetracker.ui.util.toCurrencyString
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ViewGroupFragment: Fragment(), MenuProvider {

    private val TAG = ViewGroupFragment::class.simpleName

    private var _binding: ViewGroupLayoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewGroupViewModel by viewModels()
    private lateinit var navController: NavController

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
        navController = Navigation.findNavController(view)
        binding.btnViewHistory.setOnClickListener {
            val group = viewModel.getStoredGroup()
            group?.let {
                navController.navigate(R.id.action_view_group_to_history_list, Bundle().apply {
                    putParcelable(Constants.ARG_GROUP, GroupModelParcel(group))
                })
            }
        }
        binding.addHistory.setOnClickListener {
            val target = binding.buttonsLayout
            if (target.isVisible()) {
                target.visibilityGone()
            }
            else {
                target.visible()
            }
        }
        binding.btnAddDebit.setOnClickListener {
            val group = viewModel.getStoredGroup()
            group?.let {
                navController.navigate(R.id.action_view_group_to_add_history,Bundle().apply {
                    putString(Constants.ARG_ACTION,Constants.ACTION_CREATE)
                    putParcelable(Constants.ARG_GROUP, GroupModelParcel(group))
                    putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, HistoryType.DEBIT)
                })
            }
        }
        binding.btnAddCredit.setOnClickListener {
            val group = viewModel.getStoredGroup()
            group?.let {
                navController.navigate(R.id.action_view_group_to_add_history,Bundle().apply {
                    putString(Constants.ARG_ACTION,Constants.ACTION_CREATE)
                    putParcelable(Constants.ARG_GROUP, GroupModelParcel(group))
                    putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, HistoryType.CREDIT)
                })
            }
        }
        viewModel.findGroupById(getArgGroupId()).observe(viewLifecycleOwner, this::onGroupLoaded)
        lifecycleScope.launch {
            viewModel.resultState.filterNotNull().collect {
                onGroupDeleted(it)
                viewModel.emptyResult()
            }
        }
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner)
    }

    private fun onGroupLoaded(group: GroupModel?) {
        if (null == group) {
            Toast.makeText(requireContext(), R.string.message_group_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        else {
            binding.name.text = group.name
            binding.balance.text = group.balance!!.toCurrencyString()
            requireActivity().invalidateOptionsMenu()
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

    private fun onClickDelete() {
        val group = viewModel.getStoredGroup()
        group?.let {
            MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.baseline_warning_64)
                .setMessage(resources.getQuantityString(R.plurals.warning_delete_group, 1, group.name))
                .setPositiveButton(R.string.label_no, null)
                .setNegativeButton(R.string.label_yes) { _,_ -> viewModel.removeGroup(group) }
                .show()
        }
    }

    private fun onGroupDeleted(result: OperationResult<GroupModel>?) {
        result?.let {
            if (result.isFailure()) {
                Log.e(TAG,"onGroupDeleted delete filed group=${viewModel.getStoredGroup()}",result.error)
            }
            else {
                navController.popBackStack()
            }
        }
    }

    private fun onClickEdit() {
        navController.navigate(R.id.action_view_group_to_edit_group, Bundle().apply {
            putString(Constants.ARG_ACTION,Constants.ACTION_EDIT)
            putLong(Constants.ARG_ID, getArgGroupId())
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}