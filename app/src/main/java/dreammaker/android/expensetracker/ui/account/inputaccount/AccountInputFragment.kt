package dreammaker.android.expensetracker.ui.account.inputaccount

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
import dreammaker.android.expensetracker.databinding.InputAccountBinding
import dreammaker.android.expensetracker.ui.UIState
import dreammaker.android.expensetracker.util.getArgId
import dreammaker.android.expensetracker.util.hasArgument
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account

class AccountInputFragment : Fragment() {

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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveAccountState.collectLatest{ state ->
                when(state) {
                    is UIState.UISuccess -> {
                        QuickMessages.toastSuccess(requireContext(),getString(R.string.message_success_save_account))
                        navController.popBackStack()
                    }
                    is UIState.UIError -> {
                        QuickMessages.simpleAlertError(requireContext(),R.string.message_fail_save_account)
                    }
                    else -> {}
                }
            }
        }
        if (isActionEdit()) {
            if (viewModel.getStoredAccount() == null) {
                val id = requireArguments().getLong(Constants.ARG_ID)
                viewModel.findAccountById(id).observe(viewLifecycleOwner, this::onAccountLoaded)
            }
        }
    }

    private fun onAccountLoaded(account: Account?) {
        if (null == account) {
            Toast.makeText(requireContext(), R.string.message_account_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        else {
            binding.name.setText(account.name)
            binding.balance.setText(account.balance.toString())
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

    private fun validateInput(account: Account): Boolean {
        var hasError = false
        binding.nameInputLayout.error = null
        binding.balanceInputLayout.error = null
        if (account.name.isBlank()) {
            binding.nameInputLayout.error = getString(R.string.error_empty_account_name_input)
            hasError = true
        }
//        if (null == account.balance){
//            binding.balanceInputLayout.error = getString(R.string.error_invalid_balance_input)
//            hasError = true
//        }
        return hasError
    }

    private fun getInputAccount(): Account {
        val name = binding.name.text.toString()
        val balanceText = binding.balance.text.toString()
        val balance = when (balanceText.isNotBlank()) {
            true -> balanceText.toFloat()
            else -> 0f
        }
        val id = when(isActionEdit()) {
            true -> getArgId()
            else -> 0L
        }
        return Account(name,balance,id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}