package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
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

class HistoryInputViewModel(app: Application) : AndroidViewModel(app) {

    private val TAG = HistoryInputViewModel::class.simpleName

    private val historyDao: HistoryDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
    }

    private val _historyState = MutableStateFlow<UIState?>(null)
    val historyState: Flow<UIState?> get() = _historyState
    val history: HistoryModel?
        get() {
            val state = _historyState.value
            return when(state) {
                is UIState.UISuccess -> state.asData()
                else -> null
            }
        }

    fun findHistory(id: Long, type: HistoryType) {
        viewModelScope.launch(Dispatchers.IO) {
            _historyState.tryEmit(UIState.UILoading())
            flow {
                val history = historyDao.findHistoryByIdAndType(id,type)
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

    fun addHistory(history: HistoryModel) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _saveHistoryState.tryEmit(UIState.UILoading())
                val id = historyDao.insertHistory(history.toHistory())
                val copy = history.copy(id=id)
                emit(copy)
            }
                .catch { error -> _saveHistoryState.tryEmit(UIState.UIError(error,history)) }
                .collect { _saveHistoryState.tryEmit(UIState.UISuccess(it)) }
        }
    }

    fun setHistory(history: HistoryModel) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _saveHistoryState.tryEmit(UIState.UILoading())
                historyDao.updateHistory(history.toHistory())
                emit(history)
            }
                .catch { error -> _saveHistoryState.tryEmit(UIState.UIError(error,history)) }
                .collect { _saveHistoryState.tryEmit(UIState.UISuccess(it)) }
        }
    }

    private val _dateState = MutableStateFlow<Date>(Date())
    val dateState: StateFlow<Date> = _dateState

    private val _primaryAccountState = MutableStateFlow<AccountModel?>(null)
    val primaryAccountState: StateFlow<AccountModel?> = _primaryAccountState

    private val _secondaryAccountState = MutableStateFlow<AccountModel?>(null)
    val secondaryAccountState: StateFlow<AccountModel?> = _secondaryAccountState

    private val _groupState = MutableStateFlow<GroupModel?>(null)
    val groupState: StateFlow<GroupModel?> = _groupState

    fun setDate(date: Date) {
        _dateState.tryEmit(date)
    }

    fun getDate(): Date = _dateState.value

    fun setAccount(account: AccountModel?, primary: Boolean = true) {
        Log.i(TAG,"set account $account primary $primary")
        if (primary) {
            _primaryAccountState.tryEmit(account)
        }
        else {
            _secondaryAccountState.tryEmit(account)
        }
    }

    fun getAccount(primary: Boolean = true): AccountModel? {
        return if (primary) {
            _primaryAccountState.value
        }
        else {
            _secondaryAccountState.value
        }
    }

    fun setGroup(group: GroupModel?) {
        _groupState.tryEmit(group)
    }

    fun getGroup(): GroupModel? {
        return _groupState.value
    }
}