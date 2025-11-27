package dreammaker.android.expensetracker.ui.history.viewhistory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.ui.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.History

class ViewHistoryItemViewModel(
    app: Application
): AndroidViewModel(app) {

    private val historyRepo = ExpenseRepository.getInstance(app).historyRepository

    private lateinit var historyLiveData: LiveData<History?>

    fun getStoredHistory(): History? {
        if (!::historyLiveData.isInitialized) {
            return null
        }
        return historyLiveData.value
    }

    fun findHistory(id: Long): LiveData<History?> {
        if (!::historyLiveData.isInitialized) {
            historyLiveData = historyRepo.getLiveHistoryById(id)
        }
        return historyLiveData
    }

    private val _removeHistoryState = MutableSharedFlow<UIState<Nothing>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val removeHistoryState: Flow<UIState<Nothing>> get() = _removeHistoryState

    fun removeHistory(history: History) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _removeHistoryState.tryEmit(UIState.UILoading())
                historyRepo.deleteHistory(history.id)
                emit(null)
            }
                .catch { error -> _removeHistoryState.tryEmit(UIState.UIError(error,history)) }
                .collect { _removeHistoryState.tryEmit(UIState.UISuccess()) }
        }
    }
}