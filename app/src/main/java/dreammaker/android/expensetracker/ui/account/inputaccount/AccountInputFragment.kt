package dreammaker.android.expensetracker.ui.account.inputaccount

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.InputAccountBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.setActivityTitle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class AccountInputFragment : Fragment() {
    private val TAG = AccountInputFragment::class.simpleName

    private var _binding: InputAccountBinding? = null
    private val binding get() = this._binding!!

    private lateinit var viewModel: AccountInputViewModel
    private lateinit var navController: NavController
    private val observer = Observer<AccountModel?> { onAccountLoaded(it) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[AccountInputViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.containsKey(Constants.ARG_ACTION) != true) {
            throw IllegalStateException("'${Constants.ARG_ACTION}' argument not found")
        }
    }

    private fun isActionEdit(): Boolean = arguments?.getString(Constants.ARG_ACTION) == Constants.ACTION_EDIT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InputAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        binding.btnSave.setOnClickListener{ onClickSave() }
        binding.btnCancel.setOnClickListener { navController.popBackStack() }
        lifecycleScope.launch {
            viewModel.resultFlow.filterNotNull().collect{
                onSave(it)
                viewModel.emptyResult()
            }
        }
        if (isActionEdit()) {
            if (viewModel.getStoredAccount() == null) {
                val id = requireArguments().getLong(Constants.ARG_ID)
                viewModel.findAccountById(id).observe(viewLifecycleOwner, observer)
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
            viewModel.accountLiveData.removeObserver(observer)
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
                val message = getString(
                    if (isActionEdit()) R.string.message_fail_edit_account
                    else R.string.message_fail_create_account
                )
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
            else {
                val message = getString(
                    if (isActionEdit()) R.string.message_success_edit_account
                    else R.string.message_success_create_account
                )
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setActivityTitle(getString(
            if (isActionEdit()) R.string.title_edit_account
            else R.string.title_create_account
        ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}