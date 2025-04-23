package dreammaker.android.expensetracker.ui.history.historyinput

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.ui.util.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class HistoryInputViewModel(app: Application) : AndroidViewModel(app) {

    private val historyDao: HistoryDao
    private val selections: MutableMap<String, LiveData<Any?>> = mutableMapOf()
    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
    }

    lateinit var history: LiveData<HistoryModel?>



    fun findHistory(id: Long, type: HistoryType): LiveData<HistoryModel?> {
        if (!::history.isInitialized) {
            history = historyDao.findHistoryByIdAndType(id,type)
        }
        return history
    }

    fun getStoredHistory(): HistoryModel? {
        if (!::history.isInitialized) {
            return null
        }
        return history.value
    }

    private val _resultState: MutableSharedFlow<OperationResult<HistoryModel>?> = MutableStateFlow(null)
    val resultState: Flow<OperationResult<HistoryModel>?> = _resultState

    fun emptyResult() {
        viewModelScope.launch {
            _resultState.emit(null)
        }
    }

    fun addHistory(history: HistoryModel) {
        viewModelScope.launch {
            flow {
                try {
                    val id = historyDao.insertHistory(history.toHistory())
                    val copy = history.copy(id=id)
                    emit(OperationResult(copy,null))
                }
                catch (ex: Throwable) {
                    emit(OperationResult(null,ex))
                }
            }
                .flowOn(Dispatchers.IO)
                .collect { _resultState.emit(it) }
        }
    }

    fun setHistory(history: HistoryModel) {
        viewModelScope.launch {
            flow {
                try {
                    val copy = history.copy()
                    historyDao.updateHistory(history.toHistory())
                    emit(OperationResult(copy,null))
                }
                catch (ex: Throwable) {
                    emit(OperationResult(null,ex))
                }
            }
                .flowOn(Dispatchers.IO)
                .collect { _resultState.emit(it) }
        }
    }

    fun getAccount(key: String): AccountModel? = getSelection(key) as AccountModel?

    fun getPerson(key: String): PersonModel? = getSelection(key) as PersonModel?

    fun setSelection(key: String, value: Any?) {
        (getSelectionLiveData(key) as MutableLiveData).postValue(value)
    }

    fun getSelection(key: String, defaultValue: Any? = null): Any? {
        if (selections.containsKey(key)) {
            return selections[key]!!.value
        }
        return defaultValue
    }

    fun getSelectionLiveData(key: String, defaultValue: Any? = null): LiveData<Any?> {
        if (!selections.containsKey(key)) {
            selections[key] = MutableLiveData(defaultValue)
        }
        return selections[key]!!
    }


}