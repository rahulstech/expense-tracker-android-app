package dreammaker.android.expensetracker.ui.history.viewhistory

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

class ViewHistoryItemViewModel(app: Application): AndroidViewModel(app) {

    private val historyDao: HistoryDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
    }

    private lateinit var historyLiveData: LiveData<HistoryModel?>

    fun getStoredHistory(): HistoryModel? {
        if (!::historyLiveData.isInitialized) {
            return null
        }
        return historyLiveData.value
    }

    fun findHistory(id: Long, type: HistoryType): LiveData<HistoryModel?> {
        if (!::historyLiveData.isInitialized) {
            historyLiveData = historyDao.findHistoryByIdAndType(id, type)
        }
        return historyLiveData
    }

    private val _removeHistoryState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val removeHistoryState: Flow<UIState> get() = _removeHistoryState.asSharedFlow()

    fun removeHistory(history: HistoryModel) {
        _removeHistoryState.tryEmit(UIState.UILoading())
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                historyDao.deleteHistory(history.toHistory())
                emit(history)
            }
                .catch { error -> _removeHistoryState.tryEmit(UIState.UIError(error,history)) }
                .collect { _removeHistoryState.tryEmit(UIState.UISuccess(it)) }
        }
    }
}