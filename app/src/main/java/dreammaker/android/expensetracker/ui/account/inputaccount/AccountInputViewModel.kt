package dreammaker.android.expensetracker.ui.account.inputaccount

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.AccountDao
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.util.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class AccountInputViewModel(app: Application): AndroidViewModel(app) {

    private val accountDao: AccountDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        accountDao = db.accountDao
    }

    lateinit var accountLiveData: LiveData<AccountModel?>

    fun getStoredAccount(): AccountModel? {
        if (!::accountLiveData.isInitialized) {
            return null
        }
        return accountLiveData.value
    }

    fun findAccountById(id: Long): LiveData<AccountModel?> {
        if (!::accountLiveData.isInitialized) {
            accountLiveData = accountDao.findAccountById(id)
        }
        return accountLiveData
    }

    private val _saveAccountState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val saveAccountState: Flow<UIState?> get() = _saveAccountState.asSharedFlow()

    fun addAccount(account: AccountModel) {
        _saveAccountState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                val id = accountDao.insertAccount(account.toAccount())
                emit(UIState.UISuccess(account.copy(id=id)))
            }
                .catch { error -> _saveAccountState.tryEmit(UIState.UIError(error,account)) }
                .collect {
                    _saveAccountState.tryEmit(UIState.UISuccess(it))
                }
        }
    }

    fun setAccount(account: AccountModel) {
        _saveAccountState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                accountDao.updateAccount(account.toAccount())
                emit(account)
            }
                .catch { error -> _saveAccountState.tryEmit(UIState.UIError(error)) }
                .collect { _saveAccountState.tryEmit(UIState.UISuccess(it)) }
        }
    }
}