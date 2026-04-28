package dreammaker.android.expensetracker.ui.account.inputaccount

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.model.Account
import javax.inject.Inject


data class AccountInputUIState(
    val isLoadingAccount: Boolean = false,
    val account: Account? = null,
    val accountLoadError: Throwable? = null,
    val isSaving: Boolean = false,
)


sealed interface AccountInputUIEvent {

    data class SaveSuccessful(val account: Account): AccountInputUIEvent

    data class SaveError(val cause: Throwable): AccountInputUIEvent
}

@HiltViewModel
class AccountInputViewModel @Inject constructor(
    private val accountRepo: AccountRepository
): ViewModel() {

    companion object {
        private const val TAG = "AccountInputViewModel"
    }

    private var _uiState = MutableStateFlow(AccountInputUIState())

    private val currentUIState: AccountInputUIState get() = _uiState.value

    val uiState: StateFlow<AccountInputUIState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AccountInputUIEvent>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.SUSPEND)

    val uiEvent: SharedFlow<AccountInputUIEvent> = _uiEvent.asSharedFlow()

    private var lastFoundAccId = 0L

    fun findAccountById(id: Long) {
        if (id == lastFoundAccId) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            updateLoadState(isLoadingAccount = true)
            try {
                val account = accountRepo.findAccountById(id)
                updateLoadState(isLoadingAccount = false, account = account)
                lastFoundAccId = id
            }
            catch (th: Throwable) {
                updateLoadState(isLoadingAccount = false, account = null, accountLoadError = th)
            }
        }
    }

    fun saveAccount(account: Account, isEdit: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSaveSate(isSaving = true)
            try {
                val savedAccount = if (isEdit) {
                    accountRepo.updateAccount(account)
                    account
                }
                else {
                    accountRepo.insertAccount(account)
                }
                _uiEvent.tryEmit(AccountInputUIEvent.SaveSuccessful(savedAccount))
            }
            catch (th: Throwable) {
                Log.e(TAG,"save account failed with error", th)
                _uiEvent.tryEmit(AccountInputUIEvent.SaveError(th))
            }
            finally {
                updateSaveSate(isSaving = false)
            }
        }
    }

    private fun updateLoadState(
        isLoadingAccount: Boolean = currentUIState.isLoadingAccount,
        account: Account? = currentUIState.account,
        accountLoadError: Throwable? = currentUIState.accountLoadError,
    ) {
        _uiState.update {
            it.copy(
                isLoadingAccount = isLoadingAccount,
                account = account,
                accountLoadError = accountLoadError
            )
        }
    }

    private fun updateSaveSate(
        isSaving: Boolean = currentUIState.isSaving,
    ) {
        _uiState.update {
            it.copy(isSaving = isSaving)
        }
    }
}
