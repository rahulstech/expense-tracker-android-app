package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.util.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate

class HistoryInputViewModel (
    app: Application
) : AndroidViewModel(app) {

    private val historyRepo = ExpenseRepository.getInstance(app).historyRepository

    private val TAG = HistoryInputViewModel::class.simpleName

    private val _historyState = MutableStateFlow<UIState>(UIState.UILoading())
    val historyState: Flow<UIState?> get() = _historyState
    val history: History?
        get() {
            val state = _historyState.value
            return when(state) {
                is UIState.UISuccess -> state.asData()
                else -> null
            }
        }

    fun findHistory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                val history = historyRepo.findHistoryById(id)
                emit(history)
            }
                .catch { error -> _historyState.tryEmit(UIState.UIError(error)) }
                .collectLatest { history -> _historyState.tryEmit(UIState.UISuccess(history)) }
        }
    }

    private val _saveHistoryState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val saveHistoryState: Flow<UIState> get() = _saveHistoryState.asSharedFlow()

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

    private val _primaryAccountState = MutableStateFlow<Account?>(null)
    val primaryAccountState: StateFlow<Account?> = _primaryAccountState

    private val _secondaryAccountState = MutableStateFlow<Account?>(null)
    val secondaryAccountState: StateFlow<Account?> = _secondaryAccountState

    private val _groupState = MutableStateFlow<Group?>(null)
    val groupState: StateFlow<Group?> = _groupState

    fun setDate(date: LocalDate) {
        _dateState.tryEmit(date)
    }

    fun getDate(): LocalDate = _dateState.value

    fun setAccount(account: Account?, primary: Boolean = true) {
        Log.i(TAG,"set account $account primary $primary")
        if (primary) {
            _primaryAccountState.tryEmit(account)
        }
        else {
            _secondaryAccountState.tryEmit(account)
        }
    }

    fun getAccount(primary: Boolean = true): Account? {
        return if (primary) {
            _primaryAccountState.value
        }
        else {
            _secondaryAccountState.value
        }
    }

//    var group: Group?
//        get() = _groupState.value
//        set(value) {
//            _groupState.value = value
//        }

    fun setGroup(group: Group?) {
        _groupState.tryEmit(group)
    }

    fun getGroup(): Group? {
        return _groupState.value
    }
}