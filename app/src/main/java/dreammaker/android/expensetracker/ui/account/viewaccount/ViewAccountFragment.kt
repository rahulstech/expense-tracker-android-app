package dreammaker.android.expensetracker.ui.account.viewaccount

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
import dreammaker.android.expensetracker.databinding.ViewAccountLayoutBinding
import dreammaker.android.expensetracker.ui.history.historieslist.HistoryListContainer
import dreammaker.android.expensetracker.util.AccountParcel
import dreammaker.android.expensetracker.util.UIState
import dreammaker.android.expensetracker.util.getBalanceText
import dreammaker.android.expensetracker.util.hasArgument
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account

class ViewAccountFragment: Fragment(), MenuProvider {

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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteAccountState.collectLatest { state ->
                when(state) {
                    is UIState.UISuccess -> {
                        QuickMessages.toastSuccess(requireContext(),getString(R.string.message_success_delete_account))
                        navController.popBackStack()
                    }
                    is UIState.UIError -> {
                        QuickMessages.simpleAlertError(requireContext(),R.string.message_fail_delete_account)
                    }
                    else -> {}
                }
            }
        }
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner)
    }

    private fun navigateToCreateHistory() {
        viewModel.getStoredAccount()?.let {
            navController.navigate(R.id.action_view_account_to_create_history, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
                putParcelable(Constants.ARG_ACCOUNT, AccountParcel(it))
            })
        }
    }

    private fun navigateToCreateTransferHistory() {
        viewModel.getStoredAccount()?.let {
            navController.navigate(R.id.action_view_account_to_create_transfer_history, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
                putParcelable(Constants.ARG_ACCOUNT, AccountParcel(it))
            })
        }
    }

    private fun handleClickViewHistory() {
        val account = viewModel.getStoredAccount()
        account?.let {
            navController.navigate(R.id.action_view_account_to_history_list, Bundle().apply {
                putParcelable(HistoryListContainer.ARG_SHOW_HISTORY_FOR, AccountParcel(it))
            })
        }
    }

    private fun onAccountLoaded(account: Account?) {
        if (account == null) {
            QuickMessages.toastError(requireContext(),getString(R.string.message_account_not_found))
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
            QuickMessages.alertWarning(requireContext(),
                getString(R.string.message_warning_delete,account.name),
                QuickMessages.AlertButton(getString(R.string.label_no)),
                QuickMessages.AlertButton(getString(R.string.label_yes)){
                    viewModel.removeAccount(account)
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}