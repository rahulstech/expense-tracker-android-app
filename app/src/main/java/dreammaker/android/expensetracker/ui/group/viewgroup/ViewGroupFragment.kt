package dreammaker.android.expensetracker.ui.group.viewgroup

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.isVisible
import dreammaker.android.expensetracker.ui.util.putHistoryType
import dreammaker.android.expensetracker.ui.util.toCurrencyString
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ViewGroupFragment: Fragment() {

    private val TAG = ViewGroupFragment::class.simpleName

    private var _binding: ViewGroupLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewGroupViewModel
    private lateinit var navController: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[ViewGroupViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewGroupLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    private fun getArgGroupId(): Long = requireArguments().getLong(Constants.ARG_ID)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
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
            navController.navigate(R.id.action_view_group_to_add_history,Bundle().apply {
                putString(Constants.ARG_ACTION,Constants.ACTION_CREATE)
                putLong(HistoryInputFragment.ARG_GROUP, getArgGroupId())
                putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, HistoryType.DEBIT)
            })
        }
        binding.btnAddCredit.setOnClickListener {
            navController.navigate(R.id.action_view_group_to_add_history,Bundle().apply {
                putString(Constants.ARG_ACTION,Constants.ACTION_CREATE)
                putLong(HistoryInputFragment.ARG_GROUP, getArgGroupId())
                putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, HistoryType.DEBIT)
            })
        }
        viewModel.findGroupById(getArgGroupId()).observe(viewLifecycleOwner, this::onGroupLoaded)
        lifecycleScope.launch {
            viewModel.resultState.filterNotNull().collect {
                onGroupDeleted(it)
                viewModel.emptyResult()
            }
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        viewModel.getStoredGroup()?.let {
            inflater.inflate(R.menu.view_person_menu, menu)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                onClickDelete()
                true
            }
            R.id.edit -> {
                onClickEdit()
                true
            }
            else -> return super.onOptionsItemSelected(item)
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