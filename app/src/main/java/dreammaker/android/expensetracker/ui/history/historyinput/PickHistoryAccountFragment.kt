package dreammaker.android.expensetracker.ui.history.historyinput

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.ui.account.AccountChooserFragment
import dreammaker.android.expensetracker.ui.util.ARG_DESTIATION_LABEL
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.setActivityTitle

class PickerHistoryAccountFragment: AccountChooserFragment(SelectionMode.SINGLE) {

    private val TAG = PickerHistoryAccountFragment::class.simpleName

    companion object {
        val ARG_KEY_SELECTED_ACCOUNT = "arg.key_selected_account"
        val SELECTED_SRC_ACCOUNT = "selectedSrcAccount"
        val SELECTED_DEST_ACCOUNT = "selectedDestAccount"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[HistoryInputViewModel::class.java]
        if (arguments?.containsKey(ARG_DESTIATION_LABEL) == true) {
            setActivityTitle(arguments?.getString(ARG_DESTIATION_LABEL) as CharSequence)
        }
    }

    override fun onAccountsLoaded(accounts: List<AccountModel>?) {
        super.onAccountsLoaded(accounts)
        togglePickerButtonVisibility()
    }

    override fun handleAccountSelection(
        store: SelectionStore<Long>,
        key: Long,
        position: Int,
        selected: Boolean
    ) {
        super.handleAccountSelection(store, key, position, selected)
        togglePickerButtonVisibility()
    }

    private fun togglePickerButtonVisibility() {
        if (viewModel.accountSelectionStore?.hasSelection() == true) {
            binding.btnChoose.show()
        }
        else {
            binding.btnChoose.hide()
        }
    }

    override fun handlePickAccount() {
        val historyInputVM = viewModel as HistoryInputViewModel
        val keySelectedAccount = requireArguments().getString(ARG_KEY_SELECTED_ACCOUNT)
        val selectedAccount = viewModel.accountSelectionStore?.selectedKey?.let { getAccountForKey(it) }
        if (keySelectedAccount == SELECTED_SRC_ACCOUNT) {
            historyInputVM.selectedSrcAccount.value = selectedAccount
        }
        else if (keySelectedAccount == SELECTED_DEST_ACCOUNT) {
            historyInputVM.selectedDestAccount.value = selectedAccount
        }
        navController.popBackStack()
    }
}