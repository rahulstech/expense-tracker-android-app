package dreammaker.android.expensetracker.ui.account.accountlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.ui.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Account
import kotlin.time.Duration.Companion.milliseconds

class AccountsListViewModel (app: Application): AndroidViewModel(app) {

    private val accountRepo = ExpenseRepository.getInstance(app).accountRepository

    private val searchTextState = MutableStateFlow<String?>(null)
    private var _accounts: LiveData<List<Account>>? = null

    var searchText: String?
        get() = searchTextState.value
        set(value) {
            searchTextState.value = value
        }

    @OptIn(FlowPreview::class)
    fun getAllAccounts(): LiveData<List<Account>> {
        if (null==_accounts) {
            _accounts = searchTextState
                .debounce(150.milliseconds)
                .asLiveData().switchMap { searchText->
                    accountRepo.getLiveAllAccounts().map { accounts ->
                        if (searchText.isNullOrBlank()) {
                            accounts
                        }
                        else {
                            accounts.filter { account -> account.name.contains(searchText,true) }
                        }
                    }
                }
        }
        return _accounts!!
    }

    private val _deleteAccountsState = MutableSharedFlow<UIState<Nothing>>(
        replay = 0, // from newest to oldest how many most recent value to keep, new collectors can collect these value even if some other already consumes
        extraBufferCapacity = 1, // how many unconsumed new values to store
        onBufferOverflow = BufferOverflow.DROP_OLDEST // if replay+extraBufferCapacity exceeds what to do, here drop the oldest values
    )
    val deleteAccountsState: Flow<UIState<Nothing>?> get() = _deleteAccountsState.asSharedFlow()

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
                    _deleteAccountsState.tryEmit(UIState.UISuccess<Nothing>())
                }
        }
    }
}