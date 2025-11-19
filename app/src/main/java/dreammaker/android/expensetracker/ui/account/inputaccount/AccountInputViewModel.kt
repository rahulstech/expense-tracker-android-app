package dreammaker.android.expensetracker.ui.account.inputaccount

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.util.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Account

class AccountInputViewModel(
    app: Application
): AndroidViewModel(app) {

    private val accountRepo = ExpenseRepository.getInstance(app).accountRepository

    lateinit var accountLiveData: LiveData<Account?>

    fun getStoredAccount(): Account? {
        if (!::accountLiveData.isInitialized) {
            return null
        }
        return accountLiveData.value
    }

    fun findAccountById(id: Long): LiveData<Account?> {
        if (!::accountLiveData.isInitialized) {
            accountLiveData = accountRepo.getLiveAccountById(id)
        }
        return accountLiveData
    }

    private val _saveAccountState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val saveAccountState: Flow<UIState?> get() = _saveAccountState.asSharedFlow()

    fun addAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _saveAccountState.tryEmit(UIState.UILoading())
                val savedAccount = accountRepo.insertAccount(account)
                emit(UIState.UISuccess(savedAccount))
            }
                .catch { error -> _saveAccountState.tryEmit(UIState.UIError(error,account)) }
                .collect {
                    _saveAccountState.tryEmit(UIState.UISuccess(it))
                }
        }
    }

    fun setAccount(account: Account) {
        _saveAccountState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                accountRepo.updateAccount(account)
                emit(account)
            }
                .catch { error -> _saveAccountState.tryEmit(UIState.UIError(error)) }
                .collect { _saveAccountState.tryEmit(UIState.UISuccess(it)) }
        }
    }
}