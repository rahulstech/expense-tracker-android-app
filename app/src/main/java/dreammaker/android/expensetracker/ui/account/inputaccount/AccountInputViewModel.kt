package dreammaker.android.expensetracker.ui.account.inputaccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.ui.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.model.Account
import javax.inject.Inject

@HiltViewModel
class AccountInputViewModel @Inject constructor(
    private val accountRepo: AccountRepository
): ViewModel() {

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

    private val _saveAccountState = MutableSharedFlow<UIState<Account>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val saveAccountState: Flow<UIState<Account>?> get() = _saveAccountState

    fun addAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _saveAccountState.tryEmit(UIState.UILoading())
                val savedAccount = accountRepo.insertAccount(account)
                emit(savedAccount)
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