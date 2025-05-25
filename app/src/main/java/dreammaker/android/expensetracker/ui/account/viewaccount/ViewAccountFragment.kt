package dreammaker.android.expensetracker.ui.account.viewaccount

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
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.databinding.ViewAccountLayoutBinding
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryListContainer
import dreammaker.android.expensetracker.ui.util.AccountModelParcel
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.OperationResult
import dreammaker.android.expensetracker.ui.util.getBalanceText
import dreammaker.android.expensetracker.ui.util.hasArgument
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViewAccountFragment: Fragment(), MenuProvider {

    private val TAG = ViewAccountFragment::class.simpleName

    private var _binding: ViewAccountLayoutBinding? = null
    private val binding get() = _binding!!
    private val navController: NavController by lazy {  findNavController() }
    private val viewModel: ViewAccountViewModel by viewModels()

    private fun getArgAccountId(): Long = requireArguments().getLong(Constants.ARG_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasArgument(Constants.ARG_ID)) {
            throw IllegalStateException("'${Constants.ARG_ID}' argument not found")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewAccountLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.addHistory.setOnClickListener { navigateToCreateHistory() }
        binding.btnAddTransfer.setOnClickListener { navigateToCreateTransferHistory() }
        binding.btnViewHistory.setOnClickListener { handleClickViewHistory() }
        viewModel.findAccountById(getArgAccountId()).observe(viewLifecycleOwner, this::onAccountLoaded)
        lifecycleScope.launch {
            viewModel.resultFlow.collectLatest {
                onAccountDeleted(it)
                viewModel.emptyResult()
            }
        }
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner)
    }

    private fun navigateToCreateHistory() {
        viewModel.getStoredAccount()?.let {
            navController.navigate(R.id.action_view_account_to_create_history, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
                putParcelable(Constants.ARG_ACCOUNT, AccountModelParcel(it))
            })
        }
    }

    private fun navigateToCreateTransferHistory() {
        viewModel.getStoredAccount()?.let {
            navController.navigate(R.id.action_view_account_to_create_transfer_history, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
                putParcelable(Constants.ARG_ACCOUNT, AccountModelParcel(it))
            })
        }
    }

    private fun handleClickViewHistory() {
        val account = viewModel.getStoredAccount()
        account?.let {
            navController.navigate(R.id.action_view_account_to_history_list, Bundle().apply {
                putParcelable(HistoryListContainer.ARG_SHOW_HISTORY_FOR, AccountModelParcel(it))
            })
        }
    }

    private fun onAccountLoaded(account: AccountModel?) {
        if (account == null) {
            Toast.makeText(requireContext(), R.string.message_account_not_found, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        else {
            binding.name.text = account.name
            binding.balance.text = account.getBalanceText(requireContext())
            requireActivity().invalidateOptionsMenu()
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        viewModel.getStoredAccount()?.let {
            inflater.inflate(R.menu.view_account_menu, menu)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.edit -> {
                navController.navigate(R.id.action_view_account_to_edit_account, Bundle().apply {
                    putString(Constants.ARG_ACTION, Constants.ACTION_EDIT)
                    putLong(Constants.ARG_ID, getArgAccountId())
                })
                true
            }
            R.id.delete -> {
                onClickDeleteAccount()
                true
            }
            else -> false
        }
    }

    private fun onClickDeleteAccount() {
        val account = viewModel.getStoredAccount()
        account?.let {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(resources.getQuantityString(R.plurals.warning_delete_accounts, 1, account.name))
                .setPositiveButton(R.string.label_no, null)
                .setNegativeButton(R.string.label_yes) { _,_ -> viewModel.removeAccount(account) }
                .show()
        }
    }

    private fun onAccountDeleted(result: OperationResult<AccountModel>?) {
        result?.let {
            if (result.isFailure()) {
                Log.e(TAG, "onAccountDeleted: failed accountId=${getArgAccountId()}", result.error)
            }
            else {
                navController.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}