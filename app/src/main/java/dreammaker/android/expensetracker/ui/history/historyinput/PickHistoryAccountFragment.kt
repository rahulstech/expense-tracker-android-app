package dreammaker.android.expensetracker.ui.history.historyinput

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.ui.account.AccountChooserFragment
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.SelectionMode
import dreammaker.android.expensetracker.ui.util.SelectionStore
import dreammaker.android.expensetracker.ui.util.setActivityTitle

class PickHistoryAccountFragment: AccountChooserFragment(SelectionMode.SINGLE) {

    companion object {
        private val TAG = PickHistoryAccountFragment::class.simpleName
    }

    private lateinit var historyViewModel: HistoryInputViewModel

    override fun getInitialSelections(): List<Long> {
        val account = historyViewModel.getAccount(requireArguments().getString(Constants.ARG_RESULT_KEY)!!)
        account?.let {
            return@getInitialSelections listOf(account.id!!)
        }
        return emptyList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        historyViewModel = ViewModelProvider(requireParentFragment(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[HistoryInputViewModel::class.java]
        if (arguments?.containsKey(Constants.ARG_DESTINATION_LABEL) == true) {
            setActivityTitle(arguments?.getString(Constants.ARG_DESTINATION_LABEL) as CharSequence)
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
        val selectedAccount = getSelectedAccount()
        val resultKey = requireArguments().getString(Constants.ARG_RESULT_KEY)!!
        historyViewModel.setAccount(resultKey, selectedAccount)
        navController.popBackStack()
    }
}