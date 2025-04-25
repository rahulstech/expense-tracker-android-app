package dreammaker.android.expensetracker.ui.group.inputgroup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.databinding.InputGroupBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.OperationResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class GroupInputFragment : Fragment() {

    private val TAG = GroupInputFragment::class.simpleName

    private var _binding: InputGroupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroupInputViewModel by activityViewModels()
    private lateinit var navController: NavController
    private val observer = Observer<GroupModel?> { onGroupLoaded(it) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InputGroupBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)

        binding.btnSave.setOnClickListener { onClickSave() }
        binding.btnCancel.setOnClickListener { onClickCancel() }

        if (isActionEdit()) {
            if (null == viewModel.getStoredGroup()) {
                val id = requireArguments().getLong(Constants.ARG_ID)
                viewModel.findGroupById(id).observe(viewLifecycleOwner, observer)
            }
        }
        lifecycleScope.launch {
            viewModel.resultState.filterNotNull().collect {
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
            viewModel.groupsLiveData.removeObserver(observer)
        }
    }

    private fun getArgAction(): String = arguments?.getString(Constants.ARG_ACTION) ?: ""

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
                Log.e(TAG, "onSave action=${getArgAction()}",result.error)
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

    override fun onResume() {
        super.onResume()
//        setActivityTitle(getString(
//            if (isActionEdit()) R.string.title_edit_group
//            else R.string.title_create_group
//        ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}