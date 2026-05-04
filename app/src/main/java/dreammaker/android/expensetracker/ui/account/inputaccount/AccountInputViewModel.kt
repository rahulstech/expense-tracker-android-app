package dreammaker.android.expensetracker.ui.account.inputaccount

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.model.Account
import javax.inject.Inject


data class AccountInputUIState(
    val id: Long = 0,
    val name: String = "",
    val balance: String = "",

    val nameError: Int? = null,
    val balanceError: Int? = null,

    val isLoadingAccount: Boolean = false,
    val loadingError: Throwable? = null,

    val isSaving: Boolean = false,
    val savingError: Throwable? = null,
    val savingSuccess: Boolean = false,

    val showAddMoreDialog: Boolean = false
)


@HiltViewModel
class AccountInputViewModel @Inject constructor(
    private val accountRepo: AccountRepository
): ViewModel() {

    companion object {
        private const val TAG = "AccountInputViewModel"
    }

    private var _uiState = MutableStateFlow(AccountInputUIState())
    val uiState: StateFlow<AccountInputUIState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onBalanceChange(balance: String) {
        _uiState.update { it.copy(balance = balance, balanceError = null) }
    }

    fun findAccountById(id: Long) {
        if (id == _uiState.value.id && id != 0L) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingAccount = true, loadingError = null) }
            try {
                val foundAccount = accountRepo.getAccountById(id).first()
                foundAccount?.let { account ->
                    _uiState.update {
                        it.copy(
                            id = account.id,
                            name = account.name,
                            balance = account.balance.toString(),
                            isLoadingAccount = false
                        )
                    }
                } ?: _uiState.update {
                    it.copy(
                        isLoadingAccount = false,
                        loadingError = Exception("Account not found")
                    )
                }
            }
            catch (th: Throwable) {
                Log.e(TAG,"can not find account for id $id", th)
                _uiState.update { it.copy(isLoadingAccount = false, loadingError = th) }
            }
        }
    }

    fun saveAccount() {
        val state = _uiState.value
        val nameBlank = state.name.isBlank()
        val balanceValue = state.balance.toDoubleOrNull()
        val balanceBlank = state.balance.isBlank()

        if (nameBlank || balanceBlank || balanceValue == null) {
            _uiState.update {
                it.copy(
                    nameError = if (nameBlank) R.string.error_empty_account_name_input else null,
                    balanceError = when {
                        balanceBlank -> R.string.error_empty_balance_input
                        balanceValue == null -> R.string.error_invalid_balance_input
                        else -> null
                    }
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true, savingError = null, savingSuccess = false) }
            try {
                val accountToSave = Account(
                    id = state.id,
                    name = state.name,
                    balance = state.balance.toDouble(),
                )
                if (accountToSave.id == 0L) {
                    accountRepo.createAccount(accountToSave)
                } else {
                    accountRepo.editAccount(accountToSave)
                }
                _uiState.update { it.copy(isSaving = false, savingSuccess = true) }
            }
            catch (th: Throwable) {
                Log.e(TAG,"save account failed with error", th)
                _uiState.update { it.copy(isSaving = false, savingError = th) }
            }
        }
    }

    fun onShowAddMoreDialog(show: Boolean) {
        _uiState.update { it.copy(showAddMoreDialog = show) }
    }

    fun resetInput() {
        _uiState.update {
            it.copy(
                name = "",
                balance = "",
                savingSuccess = false,
                showAddMoreDialog = false
            )
        }
    }
}
