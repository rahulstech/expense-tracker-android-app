package dreammaker.android.expensetracker.ui.account.accountlist

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

class AccountsListViewModel(app: Application): AndroidViewModel(app) {
    private val accountDao: AccountDao
    init {
        val db = ExpensesDatabase.getInstance(app)
        accountDao = db.accountDao
    }

    private lateinit var accounts: LiveData<List<AccountModel>>

    fun getAllAccounts(): LiveData<List<AccountModel>> {
        if (!::accounts.isInitialized) {
            accounts = accountDao.getAllAccounts()
        }
        return accounts
    }

    private val _deleteAccountsState = MutableSharedFlow<UIState>(
        replay = 0, // from newest to oldest how many most recent value to keep, new collectors can collect these value even if some other already consumes
        extraBufferCapacity = 1, // how many unconsumed new values to store
        onBufferOverflow = BufferOverflow.DROP_OLDEST // if replay+extraBufferCapacity exceeds what to do, here drop the oldest values
    )
    val deleteAccountsState: Flow<UIState?> get() = _deleteAccountsState.asSharedFlow()

    fun deleteAccounts(ids: List<Long>) {
        _deleteAccountsState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                accountDao.deleteMultipleAccount(ids)
                emit(null)
            }
                .catch { error ->
                    _deleteAccountsState.tryEmit(UIState.UIError(error))
                }
                .collect {
                    _deleteAccountsState.tryEmit(UIState.UISuccess())
                }
        }
    }
}