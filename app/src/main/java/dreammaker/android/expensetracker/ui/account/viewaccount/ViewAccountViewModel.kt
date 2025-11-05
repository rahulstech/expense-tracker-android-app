package dreammaker.android.expensetracker.ui.account.viewaccount

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

class ViewAccountViewModel(app: Application): AndroidViewModel(app) {

    private val accountDao: AccountDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        accountDao = db.accountDao
    }

    private lateinit var account: LiveData<AccountModel?>

    fun getStoredAccount(): AccountModel? {
        if (!::account.isInitialized) {
            return null
        }
        return account.value
    }

    fun findAccountById(id: Long): LiveData<AccountModel?> {
        if (!::account.isInitialized) {
            account = accountDao.findAccountById(id)
        }
        return account
    }

    private val _deleteAccountState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val deleteAccountState: Flow<UIState> get() = _deleteAccountState.asSharedFlow()

    fun removeAccount(account: AccountModel) {
        _deleteAccountState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                accountDao.deleteAccount(account.toAccount())
                emit(account)
            }
                .catch { error -> _deleteAccountState.tryEmit(UIState.UIError(error,account)) }
                .collect { _deleteAccountState.tryEmit(UIState.UISuccess(it)) }
        }
    }
}