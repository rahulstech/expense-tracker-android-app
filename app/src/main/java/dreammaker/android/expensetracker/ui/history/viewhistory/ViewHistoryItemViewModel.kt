package dreammaker.android.expensetracker.ui.history.viewhistory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.util.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    private val _resultState = MutableStateFlow<OperationResult<HistoryModel>?>(null)
    val resultState: Flow<OperationResult<HistoryModel>?> = _resultState

    fun emptyResult() {
        viewModelScope.launch { _resultState.emit(null) }
    }

    fun removeHistory(history: HistoryModel) {
        viewModelScope.launch {
            flow {
                try {
                    val copy = history.copy()
                    historyDao.deleteHistory(history.toHistory())
                    emit(OperationResult(copy, null))
                }
                catch (ex: Throwable) {
                    emit(OperationResult(null,ex))
                }
            }
                .flowOn(Dispatchers.IO)
                .collect{ _resultState.emit(it) }
        }
    }
}