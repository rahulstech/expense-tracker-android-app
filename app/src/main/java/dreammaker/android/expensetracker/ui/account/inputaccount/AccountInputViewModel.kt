package dreammaker.android.expensetracker.ui.account.inputaccount

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.AccountDao
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.util.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class AccountInputViewModel(app: Application): AndroidViewModel(app) {

    private val TAG = AccountInputViewModel::class.simpleName

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

    private val _resultFlow: MutableStateFlow<OperationResult<AccountModel>?> = MutableStateFlow(null)
    val resultFlow: Flow<OperationResult<AccountModel>?> = _resultFlow

    fun emptyResult() {
        viewModelScope.launch { _resultFlow.emit(null) }
    }

    fun addAccount(account: AccountModel) {
        viewModelScope.launch {
            flow {
                try {
                    val id = accountDao.insertAccount(account.toAccount())
                    val copy = account.copy(id=id)
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

    fun setAccount(account: AccountModel) {
        viewModelScope.launch {
            flow {
                try {
                    accountDao.updateAccount(account.toAccount())
                    val copy = account.copy()
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