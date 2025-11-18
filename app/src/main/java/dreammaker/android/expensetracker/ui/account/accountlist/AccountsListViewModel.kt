package dreammaker.android.expensetracker.ui.account.accountlist

import android.app.Application
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

class AccountsListViewModel (app: Application): ViewModel() {

    private val accountRepo = ExpenseRepository.getInstance(app).accountRepository

    private var accounts: LiveData<List<Account>>? = null

    fun getAllAccounts(): LiveData<List<Account>> {
        if (null==accounts) {
            accounts = accountRepo.getLiveAllAccounts()
        }
        return accounts!!
    }

    private val _deleteAccountsState = MutableSharedFlow<UIState>(
        replay = 0, // from newest to oldest how many most recent value to keep, new collectors can collect these value even if some other already consumes
        extraBufferCapacity = 1, // how many unconsumed new values to store
        onBufferOverflow = BufferOverflow.DROP_OLDEST // if replay+extraBufferCapacity exceeds what to do, here drop the oldest values
    )
    val deleteAccountsState: Flow<UIState?> get() = _deleteAccountsState.asSharedFlow()

    fun deleteAccounts(ids: List<Long>) {

        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _deleteAccountsState.tryEmit(UIState.UILoading())
                accountRepo.deleteMultipleAccounts(ids)
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