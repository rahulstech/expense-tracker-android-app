package dreammaker.android.expensetracker.ui.history.historieslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dreammaker.android.expensetracker.ui.HistoryListItem
import dreammaker.android.expensetracker.ui.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.HistoryFilterParameters
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate
import java.util.Objects

class LoadHistoryParameters {

    private val filterParams = HistoryFilterParameters()
    private var showHeaders: Boolean = false

    fun betweenDates(startInclusive: LocalDate, endInclusive: LocalDate) {
        filterParams.dateStart = startInclusive
        filterParams.dateEnd = endInclusive
    }

    fun withHeaders(showHeaders: Boolean) { this.showHeaders = showHeaders }

    fun withTypes(types: List<History.Type>) {
        filterParams.types = types
    }

    fun withAccountId(id: Long) {
        filterParams.accountIds = listOf(id)
    }

    fun withGroupId(id: Long) {
        filterParams.groupIds = listOf(id)
    }

    override fun equals(other: Any?): Boolean {
        if (this===other) return true
        if (other is LoadHistoryParameters) {
            return filterParams == other.filterParams
                    && showHeaders == other.showHeaders
        }
        return false
    }

    override fun hashCode(): Int = Objects.hash(filterParams, showHeaders)

    override fun toString(): String {
        return "LoadHistoryParameters[filterParams=$filterParams, showHeaders=$showHeaders]"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPagedHistoryListItems(repo: HistoryRepository): Flow<PagingData<HistoryListItem>> {
        Log.d("LoadHistoryParameters", "getPagedHistoryListItems: filterParams=$filterParams")
        return repo.getPagedHistories(filterParams)
            .map { data ->
                var items: PagingData<HistoryListItem> = data.map { HistoryListItem.Item(it) }
                if (showHeaders) {
                    items = items.insertSeparators { before, after ->

                        val bHistory = (before as? HistoryListItem.Item)?.data
                        val aHistory = (after as? HistoryListItem.Item)?.data

                        // case 1: before = null then first item
                        if (null == before && null != aHistory) {
                            val date = aHistory.date
                            return@insertSeparators HistoryListItem.Header(LocalDate.from(date))
                        }

                        if (null != aHistory && null != bHistory && aHistory.date != bHistory.date) {
                            val date = aHistory.date
                            return@insertSeparators HistoryListItem.Header(LocalDate.from(date))
                        }
                        null
                    }
                }
                items
            }
    }
}

class ViewHistoryViewModel(
    app: Application
): AndroidViewModel(app) {

    companion object {
        private val TAG = ViewHistoryViewModel::class.simpleName
    }

    private val historyRepo = ExpenseRepository.getInstance(app).historyRepository
    private var summaryJob: Job? = null

    private val loadHistoryParamsState = MutableStateFlow<LoadHistoryParameters?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val histories : LiveData<PagingData<HistoryListItem>> = loadHistoryParamsState.flatMapLatest {
        it?.getPagedHistoryListItems(historyRepo) ?: flowOf(PagingData.empty())
    }
        .asLiveData()
        .cachedIn(viewModelScope)

//    val historySummary: LiveData<HistorySummary> by lazy {
//        loadHistoryParametersLiveData.switchMap { params -> MutableLiveData(HistorySummary()) }
//    }

    fun loadHistories(params: LoadHistoryParameters?) {
        Log.d(TAG,"loadHistories: params=$params")
        // if filterParams change load history -> apply filter
        loadHistoryParamsState.value = params
    }

    // delete histories

    private val _deleteHistoriesState = MutableSharedFlow<UIState<Nothing>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val deleteHistoriesState: Flow<UIState<Nothing>?> get() = _deleteHistoriesState

    fun deleteHistories(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _deleteHistoriesState.tryEmit(UIState.UILoading())
                historyRepo.deleteMultipleHistories(ids)
                emit(null)
            }
                .catch { error -> _deleteHistoriesState.tryEmit(UIState.UIError(error)) }
                .collect { _deleteHistoriesState.tryEmit(UIState.UISuccess()) }
        }
    }
}