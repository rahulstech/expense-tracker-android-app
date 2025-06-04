package dreammaker.android.expensetracker.ui.group.inputgroup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.databinding.InputGroupBinding
import dreammaker.android.expensetracker.util.Constants
import dreammaker.android.expensetracker.util.OperationResult
import dreammaker.android.expensetracker.util.hasArgument
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GroupInputFragment : Fragment() {

    private val TAG = GroupInputFragment::class.simpleName

    private var _binding: InputGroupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroupInputViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasArgument(Constants.ARG_ACTION)) {
            throw IllegalStateException("'${Constants.ARG_ACTION}' argument not found")
        }
        if (getArgAction() == Constants.ACTION_EDIT && !hasArgument(Constants.ARG_ID)) {
            throw IllegalStateException("'${Constants.ARG_ID}' argument not found; it is required for ${Constants.ARG_ACTION}='${Constants.ACTION_EDIT}'")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InputGroupBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSave.setOnClickListener { onClickSave() }
        binding.btnCancel.setOnClickListener { onClickCancel() }
        if (isActionEdit()) {
            if (null == viewModel.getStoredGroup()) {
                val id = requireArguments().getLong(Constants.ARG_ID)
                viewModel.findGroupById(id).observe(viewLifecycleOwner, this::onGroupLoaded)
            }
        }
        lifecycleScope.launch {
            viewModel.resultState.collectLatest {
                onSave(it)
                viewModel.emptyState()
            }
        }
    }

    private fun onGroupLoaded(group: GroupModel?) {
        if (null == group) {
            Toast.makeText(requireContext(), R.string.message_group_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        else {
            binding.name.setText(group.name)
            binding.balance.setText(group.balance.toString())
            viewModel.groupsLiveData.removeObservers(viewLifecycleOwner)
        }
    }

    private fun getArgAction(): String? = arguments?.getString(Constants.ARG_ACTION)

    private fun isActionEdit(): Boolean = getArgAction() == Constants.ACTION_EDIT

    private fun onClickSave() {
        val group = getInputGroup()
        if (!validateInput(group)) {
            if (isActionEdit()) {
                viewModel.setGroup(group)
            }
            else {
                viewModel.addGroup(group)
            }
        }
    }

    private fun onClickCancel() {
        navController.popBackStack()
    }

    private fun validateInput(group: GroupModel): Boolean {
        binding.nameInput.error = null
        binding.balanceInput.error = null
        var hasError = false
        if (group.name.isNullOrBlank()) {
            hasError = true
            binding.nameInput.error = getString(R.string.error_empty_group_name)
        }
        if (null == group.balance) {
            hasError = true
            binding.balanceInput.error = getString(R.string.error_invalid_group_balance_input)
        }
        return hasError
    }

    private fun getInputGroup(): GroupModel {
        val name = binding.name.text.toString()
        val balance = binding.balance.text.toString().toFloatOrNull()
        return if (isActionEdit()) {
            viewModel.getStoredGroup()!!.copy(name=name, balance=balance)
        }
        else {
            GroupModel(null,name,balance)
        }
    }

    private fun onSave(result: OperationResult<GroupModel>?) {
        result?.let {
            if (result.isFailure()) {
                Log.e(TAG, "onSave: action=${getArgAction()}",result.error)
                val message = getString(
                    if (isActionEdit()) R.string.message_fail_edit_group
                    else R.string.message_fail_create_group
                )
                Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
            }
            else {
                val message = getString(
                    if (isActionEdit()) R.string.message_success_edit_group
                    else R.string.message_success_create_group
                )
                Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}