package dreammaker.android.expensetracker.ui.account.viewaccount

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.AccountDao
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.ui.util.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ViewAccountViewModel(app: Application): AndroidViewModel(app) {

    private val TAG = ViewAccountViewModel::class.simpleName

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

    private val _resultFlow: MutableStateFlow<OperationResult<AccountModel>?> = MutableStateFlow(null)
    val resultFlow: Flow<OperationResult<AccountModel>?> = _resultFlow

    fun emptyResult() {
        viewModelScope.launch { _resultFlow.emit(null) }
    }

    fun removeAccount(account: AccountModel) {
        viewModelScope.launch {
            flow {
                try {
                    val copy = account.copy()
                    accountDao.deleteAccount(account.toAccount())
                    emit(OperationResult(copy,null))
                }
                catch (ex: Throwable) {
                    emit(OperationResult(null,ex))
                }
            }
                .flowOn(Dispatchers.IO)
                .collect { _resultFlow.emit(it) }
        }
    }
}