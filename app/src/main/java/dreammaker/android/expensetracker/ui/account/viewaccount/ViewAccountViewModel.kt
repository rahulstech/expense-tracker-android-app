package dreammaker.android.expensetracker.ui.account.viewaccount

import android.app.Application
import android.util.Log
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
    fun findAccountById(id: Long): LiveData<AccountModel?> {
        if (!::account.isInitialized) {
            account = accountDao.findAccountById(id)
        }
        return account
    }

    private val _resultFlow: MutableStateFlow<OperationResult<AccountModel>?> = MutableStateFlow(null)
    val resultFlow: Flow<OperationResult<AccountModel>?> = _resultFlow

    fun removeAccount(account: AccountModel) {
        viewModelScope.launch {
            flow {
                try {
                    val copy = account.copy()
                    val changes = accountDao.deleteAccount(account.toAccount())
                    Log.i(TAG, "removeAccount: account=$account changes=$changes")
                    emit(OperationResult(copy,null))
                }
                catch (ex: Throwable) {
                    emit(OperationResult(null,ex))
                }
            }
                .flowOn(Dispatchers.IO)
                .collect {
                    _resultFlow.value = it
                }
        }
    }
}