package dreammaker.android.expensetracker.ui.account.viewaccount

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.ui.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Account

class ViewAccountViewModel(
   app: Application
): AndroidViewModel(app) {

    private val accountRepo = ExpenseRepository.getInstance(app).accountRepository

    private lateinit var account: LiveData<Account?>

    fun getStoredAccount(): Account? {
        if (!::account.isInitialized) {
            return null
        }
        return account.value
    }

    fun findAccountById(id: Long): LiveData<Account?> {
        if (!::account.isInitialized) {
            account = accountRepo.getLiveAccountById(id)
        }
        return account
    }

    private val _deleteAccountState = MutableSharedFlow<UIState<Account>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val deleteAccountState: Flow<UIState<Account>> get() = _deleteAccountState

    fun removeAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _deleteAccountState.tryEmit(UIState.UILoading())
                accountRepo.deleteAccount(account.id)
                emit(account)
            }
                .catch { error -> _deleteAccountState.tryEmit(UIState.UIError(error,account)) }
                .collect { _deleteAccountState.tryEmit(UIState.UISuccess(it)) }
        }
    }

    fun toggleDefaultAccount(account: Account) {
        if (account.isDefault) {
            accountRepo.removeDefaultAccount()
        }
        else {
            accountRepo.setDefaultAccount(account)
        }
    }
}