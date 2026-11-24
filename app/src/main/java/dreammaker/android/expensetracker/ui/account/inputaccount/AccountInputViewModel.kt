package dreammaker.android.expensetracker.ui.account.inputaccount

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.model.Account
import java.math.BigDecimal
import javax.inject.Inject

sealed interface AccountInputDialog {

    data object None: AccountInputDialog

    data object PendingSave: AccountInputDialog

    data class SaveError(val cause: Throwable): AccountInputDialog

    data object AddMore: AccountInputDialog
}


@Immutable
data class AccountInputUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val id: Long = 0,
    val name: String = "",
    val balance: String = "",
    @param:StringRes val nameError: Int? = null,
    @param:StringRes val balanceError: Int? = null,
    val isSaving: Boolean = false,
    val showDialog: AccountInputDialog = AccountInputDialog.None
) {

    fun toAccount(): Account {
        val balance = balance.toBigDecimalOrNull() ?: BigDecimal.ZERO
        return Account(id=id,name=name, balance=balance)
    }
}

sealed interface AccountInputUiEvent {

    data object SaveAccount: AccountInputUiEvent

    data class NameChange(val name: String): AccountInputUiEvent

    data class BalanceChange(val balance: String): AccountInputUiEvent

    data object AddMoreAccount: AccountInputUiEvent

    data object DismissDialog: AccountInputUiEvent
}

sealed interface AccountInputUiEffect {

    data class ShowAccountLoadErrorAndExit(val cause: Throwable): AccountInputUiEffect

    data object ShowAccountNotFoundAndExit: AccountInputUiEffect

    data class ShowSaveSuccessful(val exit: Boolean): AccountInputUiEffect
}

private data class ValidationErrorResult(
    @param:StringRes val nameError: Int? = null,
    @param:StringRes val balanceError: Int? = null,
)

@HiltViewModel
class AccountInputViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val accountRepo: AccountRepository
): ViewModel() {

    companion object {
        private const val TAG = "AccountInputViewModel"
    }

    private val _uiState = MutableStateFlow(AccountInputUiState())

    val uiState: StateFlow<AccountInputUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AccountInputUiEffect>()

    val uiEffect: Flow<AccountInputUiEffect> = _uiEffect.receiveAsFlow()

    init {
        runCatching {
            val route = savedState.toRoute(AppDestination.EditAccount::class)
            loadAccountById(route.accountId)
        }
    }


    fun handleUiEvent(event: AccountInputUiEvent) {
        when(event) {
            is AccountInputUiEvent.NameChange -> {
                _uiState.update {
                    it.copy(name = event.name, nameError = null)
                }
            }
            is AccountInputUiEvent.BalanceChange -> {
                _uiState.update {
                    it.copy(balance = event.balance, balanceError = null)
                }
            }
            is AccountInputUiEvent.SaveAccount -> {
                saveAccount()
            }
            is AccountInputUiEvent.AddMoreAccount -> {
                _uiState.update {
                    it.copy(
                        name = "", balance = "",
                        nameError = null, balanceError = null,
                        showDialog = AccountInputDialog.None
                    )
                }
            }
            is AccountInputUiEvent.DismissDialog -> {
                _uiState.update {
                    it.copy(showDialog = AccountInputDialog.None)
                }
            }
        }
    }

    fun loadAccountById(id: Long) {
        viewModelScope.launch {
            accountRepo.getAccountById(id)
                .catch { cause ->
                    _uiEffect.send(AccountInputUiEffect.ShowAccountLoadErrorAndExit(cause))
                }
                .collectLatest {
                    it?.let { account ->
                        _uiState.update { state ->
                            state.copy(
                                isEditing = true,
                                id = account.id,
                                name = account.name,
                                balance = account.balance.toString(),
                                isLoading = false
                            )
                        }
                    } ?: _uiEffect.send(AccountInputUiEffect.ShowAccountNotFoundAndExit)
                }
        }
    }

    private fun saveAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true, showDialog = AccountInputDialog.PendingSave) }
            val state = _uiState.value
            val result = validateInput(state)
            if (result == null) {
                val account = state.toAccount()
                runCatching {
                    if (state.isEditing) {
                        accountRepo.editAccount(account)
                    }
                    else {
                        accountRepo.createAccount(account)
                    }
                }
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                showDialog = if (state.isEditing) AccountInputDialog.None else AccountInputDialog.AddMore
                            )
                        }
                        _uiEffect.send(AccountInputUiEffect.ShowSaveSuccessful(state.isEditing))
                    }
                    .onFailure { cause ->
                        Log.e(TAG,"account save error", cause)
                        _uiState.update { it.copy(isSaving = false, showDialog = AccountInputDialog.SaveError(cause)) }
                    }
            }
            else {
                _uiState.update {
                    it.copy(
                        isSaving = true,
                        nameError = result.nameError,
                        balanceError = result.balanceError,
                        showDialog = AccountInputDialog.None
                    )
                }
            }
        }
    }

    private fun validateInput(state: AccountInputUiState): ValidationErrorResult? {
        val balance = state.balance.toBigDecimalOrNull()
        val nameError: Int? = if (state.name.isBlank()) R.string.error_empty_input else null
        val balanceError: Int? = if (state.balance.isNotBlank() && balance != null) R.string.error_non_numeric_input else null

        if (nameError == null && balanceError == null) {
            return null
        }

        return ValidationErrorResult(nameError,balanceError)
    }
}
