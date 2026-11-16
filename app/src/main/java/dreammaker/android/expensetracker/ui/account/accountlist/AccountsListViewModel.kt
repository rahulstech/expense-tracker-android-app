package dreammaker.android.expensetracker.ui.account.accountlist

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.model.Account
import javax.inject.Inject


sealed interface AccountListDialog {

    data object None: AccountListDialog

    data object ConfirmDelete: AccountListDialog

    data class DeleteError(val cause: Throwable): AccountListDialog

    data object PendingDelete: AccountListDialog
}

@Immutable
data class AccountListUiState(
    val isLoading: Boolean = false,
    val allAccounts: List<Account> = emptyList(),
    val isSelectingAccounts: Boolean = false,
    val selectedAccountIds: Set<Long> = emptySet(),
    val queryText: String = "",
    val showDialog: AccountListDialog = AccountListDialog.None
) {
    val accountSelectionCount: Int
        get() = selectedAccountIds.size

    fun toggleAccountSelection(account: Account): AccountListUiState {
        val newSelection = if (account.id in selectedAccountIds) {
            selectedAccountIds - account.id
        } else {
            selectedAccountIds + account.id
        }

        return copy(selectedAccountIds = newSelection)
    }

    fun isAccountSelected(account: Account): Boolean =
        account.id in selectedAccountIds

    fun clearAccountSelection(): AccountListUiState =
        copy(selectedAccountIds = emptySet())
}


sealed interface AccountListUiEvent {

    data class AccountSearchTextChange(val queryText: String): AccountListUiEvent

    data class ToggleAccountSelection(val account: Account): AccountListUiEvent

    data object ExitAccountsSelection: AccountListUiEvent

    data object DeleteSelectedAccounts: AccountListUiEvent

    data object ConfirmDeleteSelectedAccount: AccountListUiEvent

    data object DismissDialog: AccountListUiEvent
}

sealed interface AccountListUiEffect {

    data object ShowDeleteAccountsSuccessful: AccountListUiEffect
}

@OptIn(FlowPreview::class)
@HiltViewModel
class AccountsListViewModel @Inject constructor(
    private val accountRepo: AccountRepository
): ViewModel() {

    companion object {
        val TAG = AccountsListViewModel::class.simpleName
    }

    private val _uiState = MutableStateFlow(AccountListUiState())

    val uiState: StateFlow<AccountListUiState> = _uiState.asStateFlow()

    private val _queryText = MutableStateFlow("")

    private val _uiEffect = Channel<AccountListUiEffect>()

    val uiEffect: Flow<AccountListUiEffect> = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                accountRepo.getAllAccounts(),
                _queryText.debounce(150L)
            ){ accounts, query ->
                if (query.isBlank()) {
                    accounts
                }
                else {
                    val filteredAccounts = accounts.filter { account ->
                        account.name.contains(query, ignoreCase = true)
                    }
                    filteredAccounts
                }
            }
                .flowOn(Dispatchers.IO)
                .collectLatest { filteredAccounts ->
                    _uiState.update {
                        it.copy(allAccounts = filteredAccounts)
                    }
            }
        }
    }

    fun handleEvent(event: AccountListUiEvent) {
        when (event) {
            is AccountListUiEvent.AccountSearchTextChange -> {
                _queryText.value = event.queryText
                _uiState.update { it.copy(queryText = event.queryText) }
            }
            is AccountListUiEvent.ToggleAccountSelection -> {
                _uiState.update {
                    it.copy(isSelectingAccounts = true)
                        .toggleAccountSelection(event.account)
                }
            }
            is AccountListUiEvent.ExitAccountsSelection -> {
                _uiState.update {
                    it.copy(
                        isSelectingAccounts = false,
                        showDialog = AccountListDialog.None
                    ).clearAccountSelection()
                }
            }
            is AccountListUiEvent.DeleteSelectedAccounts -> {
                _uiState.update {
                    it.copy(showDialog = AccountListDialog.ConfirmDelete)
                }
            }
            is AccountListUiEvent.ConfirmDeleteSelectedAccount -> {
                deleteAccounts(_uiState.value.selectedAccountIds.toList())
            }
            is AccountListUiEvent.DismissDialog -> {
                _uiState.update {
                    it.copy(showDialog = AccountListDialog.None)
                }
            }
        }
    }


    private fun deleteAccounts(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(showDialog = AccountListDialog.PendingDelete) }
            try {
                accountRepo.removeMultipleAccounts(ids)

                _uiState.update {
                    it.copy(
                        isSelectingAccounts = false,
                        showDialog = AccountListDialog.None
                    ).clearAccountSelection()
                }
                _uiEffect.send(AccountListUiEffect.ShowDeleteAccountsSuccessful)
            }
            catch (cause: Throwable) {
                Log.e(TAG, "delete selected account error", cause)
                _uiState.update {
                    it.copy(
                        showDialog = AccountListDialog.DeleteError(cause)
                    )
                }
            }
        }
    }
}