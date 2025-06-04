package dreammaker.android.expensetracker.ui.account.inputaccount

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
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.InputAccountBinding
import dreammaker.android.expensetracker.util.Constants
import dreammaker.android.expensetracker.util.OperationResult
import dreammaker.android.expensetracker.util.hasArgument
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountInputFragment : Fragment() {
    private val TAG = AccountInputFragment::class.simpleName

    private var _binding: InputAccountBinding? = null
    private val binding get() = this._binding!!

    private val viewModel: AccountInputViewModel by viewModels()
    private val navController: NavController by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasArgument(Constants.ARG_ACTION)) {
            throw IllegalStateException("'${Constants.ARG_ACTION}' argument not found")
        }
        if (getAction() == Constants.ACTION_EDIT && !hasArgument(Constants.ARG_ID)) {
            throw IllegalStateException("'${Constants.ARG_ID}' argument not found; it is required for ${Constants.ARG_ACTION} = '${Constants.ACTION_EDIT}'")
        }
    }

    private fun getAction(): String? = arguments?.getString(Constants.ARG_ACTION)

    private fun isActionEdit(): Boolean = getAction() == Constants.ACTION_EDIT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InputAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSave.setOnClickListener{ onClickSave() }
        binding.btnCancel.setOnClickListener { onClickCancel() }
        lifecycleScope.launch {
            viewModel.resultFlow.collectLatest{
                onSave(it)
                viewModel.emptyResult()
            }
        }
        if (isActionEdit()) {
            if (viewModel.getStoredAccount() == null) {
                val id = requireArguments().getLong(Constants.ARG_ID)
                viewModel.findAccountById(id).observe(viewLifecycleOwner, this::onAccountLoaded)
            }
        }
    }

    private fun onAccountLoaded(account: AccountModel?) {
        if (null == account) {
            Toast.makeText(requireContext(), R.string.message_account_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        else {
            binding.name.setText(account.name)
            binding.balance.setText(account.balance!!.toString())
            viewModel.accountLiveData.removeObservers(viewLifecycleOwner)
        }
    }

    private fun onClickSave() {
       val account = getInputAccount()
        if (!validateInput(account)) {
            if (isActionEdit()) {
                viewModel.setAccount(account)
            }
            else {
                viewModel.addAccount(account)
            }
        }
    }

    private fun onClickCancel() {
        navController.popBackStack()
    }

    private fun validateInput(account: AccountModel): Boolean {
        var hasError = false
        binding.nameInputLayout.error = null
        binding.balanceInputLayout.error = null
        if (account.name.isNullOrBlank()) {
            binding.nameInputLayout.error = getString(R.string.error_empty_account_name_input)
            hasError = true
        }
        if (null == account.balance){
            binding.balanceInputLayout.error = getString(R.string.error_invalid_balance_input)
            hasError = true
        }
        return hasError
    }

    private fun getInputAccount(): AccountModel {
        val balance = binding.balance.text.toString().toFloatOrNull()
        val name = binding.name.text.toString()
        return if (isActionEdit()) {
            viewModel.getStoredAccount()!!.copy(name=name, balance=balance)
        }
        else {
            AccountModel(null,name, balance)
        }
    }

    private fun onSave(result: OperationResult<AccountModel>?) {
        result?.let {
            if (result.isFailure()) {
                Log.e(TAG, "onSave failed action='${getAction()}' ", result.error)
                val message = getString(
                    if (isActionEdit()) R.string.message_fail_edit_account
                    else R.string.message_fail_create_account
                )
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
            else {
                if (isActionEdit()) {
                    Toast.makeText(requireContext(), R.string.message_success_edit_account, Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }
                else {
                    Toast.makeText(requireContext(),R.string.message_success_create_account, Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}