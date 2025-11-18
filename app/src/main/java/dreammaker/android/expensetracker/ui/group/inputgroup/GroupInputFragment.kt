package dreammaker.android.expensetracker.ui.group.inputgroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.databinding.InputGroupBinding
import dreammaker.android.expensetracker.util.UIState
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.hasArgument
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Group

class GroupInputFragment : Fragment() {

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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveGroupState.collectLatest { state ->
                when(state) {
                    is UIState.UISuccess -> {
                        QuickMessages.toastSuccess(requireContext(),getString(R.string.message_success_save_group))
                        navController.popBackStack()
                    }
                    is UIState.UIError -> {
                        QuickMessages.simpleAlertError(requireContext(),R.string.message_fail_save_group)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun onGroupLoaded(group: Group?) {
        if (null == group) {
            Toast.makeText(requireContext(), R.string.message_group_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        else {
            binding.name.setText(group.name)
            binding.due.setText(group.due.toString())
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

    private fun validateInput(group: Group): Boolean {
        binding.nameInput.error = null
        binding.balanceInput.error = null
        var hasError = false
        if (group.name.isBlank()) {
            hasError = true
            binding.nameInput.error = getString(R.string.error_empty_group_name)
        }
        return hasError
    }

    private fun getInputGroup(): Group {
        val id = when(isActionEdit()) {
            true -> getArgId()
            else -> 0L
        }
        val name = binding.name.text.toString()
        val balanceText = binding.due.text.toString()
        val balance = when(balanceText.isNotBlank()) {
            true -> balanceText.toFloat()
            else -> 0f
        }
        return Group(name,balance,id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}