package dreammaker.android.expensetracker.ui.history.historieslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dreammaker.android.expensetracker.util.UIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.HistoryFilterParameters
import rahulstech.android.expensetracker.domain.HistoryRepository
import java.time.LocalDate

class LoadHistoryParameters private constructor(
    internal val filterParams: HistoryFilterParameters
) {
    companion object {

        fun ofAccount(id: Long): LoadHistoryParameters =
            LoadHistoryParameters(HistoryFilterParameters.AccountHistories(id))

        fun ofGroup(id: Long): LoadHistoryParameters =
            LoadHistoryParameters(HistoryFilterParameters.GroupHistories(id))
    }

    private var hasHeaders: Boolean = false

    fun betweenDates(startInclusive: LocalDate, endInclusive: LocalDate) {
        filterParams.setDateRange(startInclusive,endInclusive)
    }

    fun withHeaders(showHeaders: Boolean) {
        hasHeaders = showHeaders
    }

    override fun equals(other: Any?): Boolean {
        if (other is LoadHistoryParameters) {
            return filterParams == other.filterParams && hasHeaders == other.hasHeaders
        }
        return false
    }

    override fun hashCode(): Int = filterParams.hashCode()

    fun createHistoriesPagingLiveData(repo: HistoryRepository, scope: CoroutineScope): LiveData<PagingData<HistoryListItem>> =
        repo.getPagedHistories(filterParams).map { pagingData ->

            var output: PagingData<HistoryListItem> = pagingData.map { HistoryListItem.Item(it) }
            if (hasHeaders) {
                output = (output as PagingData<HistoryListItem.Item>).insertSeparators { before, after ->

                    val bHistory = before?.history
                    val aHistory = after?.history

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
            output
        }
            .cachedIn(scope)
}

class ViewHistoryViewModel(
    app: Application
): AndroidViewModel(app) {

    companion object {
        private val TAG = ViewHistoryViewModel::class.simpleName
    }

    private val historyRepo = ExpenseRepository.getInstance(app).historyRepository

    private val _historyFilterDataLiveData = MutableLiveData<HistoryFilterData>(null)
    private var filterJob: Job? = null
    private var summaryJob: Job? = null

    private val loadHistoryParametersLiveData = MutableLiveData<LoadHistoryParameters?>(null)

    val histories : LiveData<PagingData<HistoryListItem>> by lazy {
        loadHistoryParametersLiveData.switchMap { params ->
            params?.createHistoriesPagingLiveData(historyRepo, viewModelScope)
        }
    }

    val historySummary: LiveData<HistorySummary> by lazy {
        loadHistoryParametersLiveData.switchMap { params -> MutableLiveData(HistorySummary()) }
    }

    fun loadHistories(params: LoadHistoryParameters?) {
        Log.d(TAG, "loadHistories params = ${params?.filterParams}")
        loadHistoryParametersLiveData.value = params
    }

//    private fun handleHistoryLiveDataSourceChanged() {
//        // cancel running filter job
//        filterJob?.cancel()
//
//        val histories = _originalHistoryLiveData?.value
//        val filterData = _historyFilterDataLiveData.value
//
//        // change ui state to loading
//        _uiStateLiveData.postValue(UIState.UILoading(""))
//
//        // if original histories is null or empty then send empty list
//        if (histories.isNullOrEmpty()) {
//            _uiStateLiveData.postValue(UIState.UISuccess(emptyList<History>()))
//            return
//        }
//
//        // is filter data is null then set original histories
//        if (null == filterData) {
//            _uiStateLiveData.postValue(UIState.UISuccess(histories))
//            return
//        }
//
//        filterJob = viewModelScope.launch(Dispatchers.Default) {
//            // before start ensure coroutine is active to avoid unnecessary computation
//            ensureActive()
//
//            Log.d(TAG, "filtering histories by filterData $filterData")
//
//            // perform actual filter
//            val filteredHistories = histories // filterData.let {  histories.filter{ history -> filterData.match(history) } }
//
//            // before returning result ensure coroutine is active to avoid posting unuseful histories
//            ensureActive()
//
//            // post the filtered histories
//            _uiStateLiveData.postValue(UIState.UISuccess(filteredHistories))
//        }
//    }
//
//    private fun handleHistorySummaryLiveDataSourceChanged() {
//        // cancel on going job
//        summaryJob?.cancel()
//
//        // if source histories is null or empty then set default summary
//        val histories = _originalHistoryLiveData?.value
//        if (histories.isNullOrEmpty()) {
//            _historySummaryLiveData.value = getDefaultHistorySummary()
//            return
//        }
//
//        // start summary calculation
//        summaryJob = viewModelScope.launch(Dispatchers.Default) {
//            var totalCredit = 0f
//            var totalDebit = 0f
//            histories.forEach { history ->
//                val amount = history.amount
//
//                when(history) {
//                    is History.CreditHistory -> {
//                        totalCredit += amount
//                    }
//                    is History.DebitHistory -> {
//                        totalDebit += amount
//                    }
//                    else -> {}
//                }
//
//            }
//            val summary = HistorySummary(totalCredit,totalDebit)
//            _historySummaryLiveData.postValue(summary)
//        }
//    }

    private fun getDefaultHistorySummary(): HistorySummary = HistorySummary()

    fun applyHistoryFilter(filterData: HistoryFilterData) {
        _historyFilterDataLiveData.value = filterData
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