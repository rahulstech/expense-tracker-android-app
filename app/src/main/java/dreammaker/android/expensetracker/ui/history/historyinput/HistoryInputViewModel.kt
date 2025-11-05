package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.util.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class HistoryInputViewModel(app: Application) : AndroidViewModel(app) {

    private val historyDao: HistoryDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
    }

    lateinit var historyLiveData: LiveData<HistoryModel?>

    fun findHistory(id: Long, type: HistoryType): LiveData<HistoryModel?> {
        if (!::historyLiveData.isInitialized) {
            historyLiveData = historyDao.findHistoryByIdAndType(id,type)
        }
        return historyLiveData
    }

    fun getStoredHistory(): HistoryModel? {
        if (!::historyLiveData.isInitialized) {
            return null
        }
        return historyLiveData.value
    }

    private val _saveHistoryState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val saveHistoryState: Flow<UIState> get() = _saveHistoryState.asSharedFlow()

    fun addHistory(history: HistoryModel) {
        _saveHistoryState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                val id = historyDao.insertHistory(history.toHistory())
                val copy = history.copy(id=id)
                emit(copy)
            }
                .catch { error -> _saveHistoryState.tryEmit(UIState.UIError(error,history)) }
                .collect { _saveHistoryState.tryEmit(UIState.UISuccess(it)) }
        }
    }

    fun setHistory(history: HistoryModel) {
        _saveHistoryState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                historyDao.updateHistory(history.toHistory())
                emit(history)
            }
                .catch { error -> _saveHistoryState.tryEmit(UIState.UIError(error,history)) }
                .collect { _saveHistoryState.tryEmit(UIState.UISuccess(it)) }
        }
    }
}