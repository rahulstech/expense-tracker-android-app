package dreammaker.android.expensetracker.ui.history.historieslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.core.util.toFirstDate
import dreammaker.android.expensetracker.core.util.toLastDate
import dreammaker.android.expensetracker.util.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate
import java.time.YearMonth

class ViewHistoryViewModel(
    app: Application
): ViewModel() {

    companion object {
        private val TAG = ViewHistoryViewModel::class.simpleName
    }

    class HistoryLoadParams private constructor(val start: LocalDate, val end: LocalDate){

        private var accountId: Long? = null

        private var groupId: Long? = null

        companion object {
            fun forMonthYear(monthYear: YearMonth) = HistoryLoadParams(monthYear.toFirstDate(), monthYear.toLastDate())

            fun forDate(date: LocalDate) = HistoryLoadParams(date, date)
        }

        fun ofAccount(id: Long): HistoryLoadParams {
            accountId = id
            return this
        }

        fun ofGroup(id: Long): HistoryLoadParams {
            groupId = id
            return this
        }

//        fun createLiveData(dao: HistoryDao): LiveData<List<HistoryModel>> {
//            return if (null != accountId) {
//                dao.getHistoriesBetweenDatesOnlyForAccount(start,end, accountId!!)
//            } else if (null != groupId) {
//                dao.getHistoriesBetweenDatesOnlyForGroup(start,end, groupId!!)
//            } else {
//                dao.getHistoriesBetweenDates(start,end)
//            }
//        }

        fun createLiveDate(repo: HistoryRepository): LiveData<List<History>> = MutableLiveData(emptyList())
    }

    private val historyRepo = ExpenseRepository.getInstance(app).historyRepository

    private val _uiStateLiveData = MediatorLiveData<UIState>()
    private val _historySummaryLiveData = MediatorLiveData<HistorySummary>()
    private var _originalHistoryLiveData: LiveData<List<History>>? = null
    private val _historyFilterDataLiveData = MutableLiveData<HistoryFilterData>(null)
    private var filterJob: Job? = null
    private var summaryJob: Job? = null

    val historySummary: LiveData<HistorySummary> get() = _historySummaryLiveData

    fun getStateLiveData(): LiveData<UIState> = _uiStateLiveData

    fun loadHistories(params: HistoryLoadParams) {
        if (_originalHistoryLiveData == null){
            val source = params.createLiveDate(historyRepo)
            _uiStateLiveData.addSource(source) { handleHistoryLiveDataSourceChanged() }
            _uiStateLiveData.addSource(_historyFilterDataLiveData) { handleHistoryLiveDataSourceChanged() }
            _historySummaryLiveData.addSource(source) { handleHistorySummaryLiveDataSourceChanged() }
            _originalHistoryLiveData = source
        }
    }

    private fun handleHistoryLiveDataSourceChanged() {
        // cancel running filter job
        filterJob?.cancel()

        val histories = _originalHistoryLiveData?.value
        val filterData = _historyFilterDataLiveData.value

        // change ui state to loading
        _uiStateLiveData.postValue(UIState.UILoading(""))

        // if original histories is null or empty then send empty list
        if (histories.isNullOrEmpty()) {
            _uiStateLiveData.postValue(UIState.UISuccess(emptyList<History>()))
            return
        }

        // is filter data is null then set original histories
        if (null == filterData) {
            _uiStateLiveData.postValue(UIState.UISuccess(histories))
            return
        }

        filterJob = viewModelScope.launch(Dispatchers.Default) {
            // before start ensure coroutine is active to avoid unnecessary computation
            ensureActive()

            Log.d(TAG, "filtering histories by filterData $filterData")

            // perform actual filter
            val filteredHistories = histories // filterData.let {  histories.filter{ history -> filterData.match(history) } }

            // before returning result ensure coroutine is active to avoid posting unuseful histories
            ensureActive()

            // post the filtered histories
            _uiStateLiveData.postValue(UIState.UISuccess(filteredHistories))
        }
    }

    private fun handleHistorySummaryLiveDataSourceChanged() {
        // cancel on going job
        summaryJob?.cancel()

        // if source histories is null or empty then set default summary
        val histories = _originalHistoryLiveData?.value
        if (histories.isNullOrEmpty()) {
            _historySummaryLiveData.value = getDefaultHistorySummary()
            return
        }

        // start summary calculation
        summaryJob = viewModelScope.launch(Dispatchers.Default) {
            var totalCredit = 0f
            var totalDebit = 0f
            histories.forEach { history ->
                val amount = history.amount

                when(history) {
                    is History.CreditHistory -> {
                        totalCredit += amount
                    }
                    is History.DebitHistory -> {
                        totalDebit += amount
                    }
                    else -> {}
                }

            }
            val summary = HistorySummary(totalCredit,totalDebit)
            _historySummaryLiveData.postValue(summary)
        }
    }

    private fun getDefaultHistorySummary(): HistorySummary = HistorySummary()

    fun applyHistoryFilter(filterData: HistoryFilterData) {
        _historyFilterDataLiveData.value = filterData
    }

    // delete histories

    private val _deleteHistoriesState = MutableSharedFlow<UIState>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val deleteHistoriesState: Flow<UIState?> get() = _deleteHistoriesState.asSharedFlow()

    fun deleteHistories(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _deleteHistoriesState.tryEmit(UIState.UILoading())
                historyRepo.deleteMultipleHistories(ids)
                emit(null)
            }
                .catch { error ->
                    _deleteHistoriesState.tryEmit(UIState.UIError(error))
                }
                .collect {
                    _deleteHistoriesState.tryEmit(UIState.UISuccess())
                }
        }
    }
}