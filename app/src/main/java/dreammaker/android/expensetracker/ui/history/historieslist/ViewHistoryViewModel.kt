package dreammaker.android.expensetracker.ui.history.historieslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.util.MonthYear
import dreammaker.android.expensetracker.util.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class ViewHistoryViewModel(app: Application): AndroidViewModel(app) {
    private val TAG = ViewHistoryViewModel::class.simpleName

    class HistoryLoadParams private constructor(val start: Date, val end: Date){

        private var accountId: Long? = null

        private var groupId: Long? = null

        companion object {
            fun forMonthYear(monthYear: MonthYear) = HistoryLoadParams(monthYear.toFirstDate(), monthYear.toLastDate())

            fun forDate(date: Date) = HistoryLoadParams(date, date)
        }

        fun ofAccount(id: Long): HistoryLoadParams {
            accountId = id
            return this
        }

        fun ofGroup(id: Long): HistoryLoadParams {
            groupId = id
            return this
        }

        fun createLiveData(dao: HistoryDao): LiveData<List<HistoryModel>> {
            return if (null != accountId) {
                dao.getHistoriesBetweenDatesOnlyForAccount(start,end, accountId!!)
            } else if (null != groupId) {
                dao.getHistoriesBetweenDatesOnlyForGroup(start,end, groupId!!)
            } else {
                dao.getHistoriesBetweenDates(start,end)
            }
        }
    }

    private val historiesDao: HistoryDao
    init {
        val db = ExpensesDatabase.getInstance(app)
        historiesDao = db.historyDao
    }

    private val _uiStateLiveData = MediatorLiveData<UIState<List<HistoryModel>>>()
    private val _historySummaryLiveData = MediatorLiveData<HistorySummary>()
    private var _originalHistoryLiveData: LiveData<List<HistoryModel>>? = null
    private val _historyFilterDataLiveData = MutableLiveData<HistoryFilterData>(null)
    private var filterJob: Job? = null
    private var summaryJob: Job? = null

    val historySummary: LiveData<HistorySummary> get() = _historySummaryLiveData

    fun getStateLiveData(): LiveData<UIState<List<HistoryModel>>> = _uiStateLiveData

    fun loadHistories(params: HistoryLoadParams) {
        if (_originalHistoryLiveData == null){
            val source = params.createLiveData(historiesDao)
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
        _uiStateLiveData.postValue(UIState.UILoading())

        // if original histories is null or empty then send empty list
        if (histories.isNullOrEmpty()) {
            _uiStateLiveData.postValue(UIState.UIData<List<HistoryModel>>(emptyList()))
            return
        }

        // is filter data is null then set original histories
        if (null == filterData) {
            _uiStateLiveData.postValue(UIState.UIData(histories))
            return
        }

        filterJob = viewModelScope.launch(Dispatchers.Default) {
            // before start ensure coroutine is active to avoid unnecessary computation
            ensureActive()

            Log.d(TAG, "filtering histories by filterData $filterData")

            // perform actual filter
            val filteredHistories = filterData.let {  histories.filter{ history -> filterData.match(history) } }

            // before returning result ensure coroutine is active to avoid posting unuseful histories
            ensureActive()

            // post the filtered histories
            _uiStateLiveData.postValue(UIState.UIData(filteredHistories))
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
                val amount = history.amount ?: return@forEach
                when(history.type) {
                    HistoryType.CREDIT -> {
                        totalCredit += amount
                    }
                    HistoryType.DEBIT -> {
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
}