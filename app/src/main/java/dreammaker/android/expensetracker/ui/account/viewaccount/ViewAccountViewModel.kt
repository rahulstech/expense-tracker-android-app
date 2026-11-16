package dreammaker.android.expensetracker.ui.account.viewaccount

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.model.Account
import javax.inject.Inject


sealed interface ViewAccountDialog {

    data object None: ViewAccountDialog

    data object ConfirmDelete: ViewAccountDialog

    data class DeleteError(val cause: Throwable): ViewAccountDialog

    data object PendingDelete: ViewAccountDialog
}


@Immutable
data class ViewAccountUiState(
    val isLoading: Boolean = false,
    val account: Account = Account.UNKNOWN,
    val isPendingAccountDelete: Boolean = true,
    val showDialog: ViewAccountDialog = ViewAccountDialog.None
)

sealed interface ViewAccountUiEvent {

    data object SetDefaultAccount: ViewAccountUiEvent

    data object DeleteAccount: ViewAccountUiEvent

    data object ConfirmDeleteAccount: ViewAccountUiEvent

    data object DismissDialog: ViewAccountUiEvent
}

sealed interface ViewAccountUiEffect {

    data object ShowAccountNotFoundAndExit: ViewAccountUiEffect

    data class ShowAccountLoadErrorAndExit(val cause: Throwable): ViewAccountUiEffect

    data class ShowAccountDeleteSuccessfulAndExit(val account: Account): ViewAccountUiEffect
}

@HiltViewModel
class ViewAccountViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val accountRepo: AccountRepository
): ViewModel() {

    companion object {
        val TAG = ViewAccountViewModel::class.simpleName
    }

    private val _uiState = MutableStateFlow(ViewAccountUiState())

    private val _uiEffect = Channel<ViewAccountUiEffect>()

    val uiState = _uiState.asStateFlow()

    val uiEffect: Flow<ViewAccountUiEffect> = _uiEffect.receiveAsFlow()

    init {
        val route = savedState.toRoute<AppDestination.ViewAccount>()
        loadAccountById(route.accountId)
    }

    private fun loadAccountById(id: Long) {
        viewModelScope.launch {
            accountRepo.getAccountById(id)
                .catch { cause ->
                    _uiEffect.send(ViewAccountUiEffect.ShowAccountLoadErrorAndExit(cause))
                }
                .onEach {
                    _uiState.update {
                        it.copy(isLoading = true)
                    }
                    delay(1000)
                }
                .collectLatest { loadedAccount ->
                    loadedAccount?.let { foundAccount ->
                        _uiState.update {
                            it.copy(isLoading = false, account = foundAccount)
                        }
                    } ?: _uiEffect.send(ViewAccountUiEffect.ShowAccountNotFoundAndExit)
                }
        }
    }

    fun handleUiEvent(event: Any) {
        when(event) {
            is ViewAccountUiEvent.SetDefaultAccount -> {}
            is ViewAccountUiEvent.DeleteAccount -> {
                _uiState.update {
                    it.copy(showDialog = ViewAccountDialog.ConfirmDelete)
                }
            }
            is ViewAccountUiEvent.ConfirmDeleteAccount -> {
                deleteAccount(uiState.value.account)
            }
            is ViewAccountUiEvent.DismissDialog -> {
                _uiState.update {
                    it.copy(showDialog = ViewAccountDialog.None)
                }
            }
        }
    }

    private fun deleteAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(isPendingAccountDelete = true, showDialog = ViewAccountDialog.PendingDelete)
            }

            try {
                accountRepo.removeAccount(account.id)
                _uiState.update {
                    it.copy(isPendingAccountDelete = false, showDialog = ViewAccountDialog.None)
                }
                _uiEffect.send(ViewAccountUiEffect.ShowAccountDeleteSuccessfulAndExit(account.copy()))
            }
            catch (cause: Throwable) {
                Log.e(TAG, "delete account error", cause)
                _uiState.update {
                    it.copy(isPendingAccountDelete = false, showDialog = ViewAccountDialog.DeleteError(cause))
                }
            }
        }
    }
}