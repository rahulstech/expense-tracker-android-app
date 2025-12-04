package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.ui.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate

class HistoryInputViewModel (
    app: Application
) : AndroidViewModel(app) {

    private val repos = ExpenseRepository.getInstance(app)
    private val historyRepo = repos.historyRepository
    private val accountRepo = repos.accountRepository

    private val _historyState = MutableStateFlow<UIState<History>>(UIState.UILoading())
    val historyState: Flow<UIState<History>?> = _historyState.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        replay = 0 // once delivered no need to redeliver
    )
    val history: History? get() =
        when(_historyState.value) {
            is UIState.UISuccess<History> -> (_historyState.value as UIState.UISuccess<History>).data
            else -> null
        }

    fun findHistory(id: Long) {
        // history found, no need to find again
        if (history?.id == id) return

        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _historyState.tryEmit(UIState.UILoading())
                val history = historyRepo.findHistoryById(id)
                emit(history)
            }
                .catch { error -> _historyState.tryEmit(UIState.UIError(error)) }
                .collectLatest { history -> _historyState.tryEmit(UIState.UISuccess(history)) }
        }
    }

    private val _saveHistoryState = MutableSharedFlow<UIState<History>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val saveHistoryState: Flow<UIState<History>> get() = _saveHistoryState

    fun addHistory(history: History) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _saveHistoryState.tryEmit(UIState.UILoading())
                val savedHistory = historyRepo.insertHistory(history)
                emit(savedHistory)
            }
                .catch { error -> _saveHistoryState.tryEmit(UIState.UIError(error,history)) }
                .collect { _saveHistoryState.tryEmit(UIState.UISuccess(it)) }
        }
    }

    fun setHistory(history: History) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _saveHistoryState.tryEmit(UIState.UILoading())
                historyRepo.updateHistory(history)
                emit(history)
            }
                .catch { error -> _saveHistoryState.tryEmit(UIState.UIError(error,history)) }
                .collect { _saveHistoryState.tryEmit(UIState.UISuccess(it)) }
        }
    }

    private val _dateState = MutableStateFlow<LocalDate>(LocalDate.now())
    val dateState: StateFlow<LocalDate> = _dateState

    private val _accountState = MutableStateFlow<Account?>(null)
    val accountState: StateFlow<Account?> = _accountState

    private val _groupState = MutableStateFlow<Group?>(null)
    val groupState: StateFlow<Group?> = _groupState

    fun setDate(date: LocalDate) {
        _dateState.tryEmit(date)
    }

    fun getDate(): LocalDate = _dateState.value

    fun setAccountSelection(account: Account?) {
        _accountState.value = account
    }

    fun getAccountSelection(): Account? = _accountState.value

    fun setGroup(group: Group?) {
        _groupState.tryEmit(group)
    }

    fun getGroup(): Group? {
        return _groupState.value
    }

    // default account

    fun setDefaultAccount(account: Account) = accountRepo.setDefaultAccount(account)

    private var _defaultAccountFlow: StateFlow<Account?>? = null

    val defaultAccount: Account? get() = _defaultAccountFlow?.value

    fun getDefaultAccount(): Flow<Account?> {
        if (null == _defaultAccountFlow) {
            _defaultAccountFlow = accountRepo.getDefaultAccount()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(),
                    initialValue = null
                )
        }
        return _defaultAccountFlow!!
    }
}