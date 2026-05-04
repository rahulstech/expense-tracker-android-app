package dreammaker.android.expensetracker.ui.history.viewhistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dreammaker.android.expensetracker.ui.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.model.History
import javax.inject.Inject

@HiltViewModel
class ViewHistoryItemViewModel @Inject constructor(
    private val historyRepo: HistoryRepository
): ViewModel() {

    private lateinit var historyLiveData: LiveData<History?>

    fun getStoredHistory(): History? {
        if (!::historyLiveData.isInitialized) {
            return null
        }
        return historyLiveData.value
    }

    fun findHistory(id: Long): LiveData<History?> {
        if (!::historyLiveData.isInitialized) {
            historyLiveData = historyRepo.getHistoryById(id).asLiveData(viewModelScope.coroutineContext + Dispatchers.IO)
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
                historyRepo.removeHistory(history.id)
                emit(null)
            }
                .catch { error -> _removeHistoryState.tryEmit(UIState.UIError(error)) }
                .collect { _removeHistoryState.tryEmit(UIState.UISuccess()) }
        }
    }
}