package dreammaker.android.expensetracker.ui.history.historieslist

import android.app.Application
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class ViewHistoryViewModel(app: Application): AndroidViewModel(app) {
    private val TAG = ViewHistoryViewModel::class.simpleName

    private class HistoriesLiveDataFactory private constructor(val start: Date, val end: Date){

        private var accountId: Long? = null

        private var groupId: Long? = null

        companion object {
            fun forMonthYear(monthYear: MonthYear) = HistoriesLiveDataFactory(monthYear.toFirstDate(), monthYear.toLastDate())

            fun forDate(date: Date) = HistoriesLiveDataFactory(date, date)
        }

        fun ofAccount(id: Long): HistoriesLiveDataFactory {
            accountId = id
            return this
        }

        fun ofGroup(id: Long): HistoriesLiveDataFactory {
            groupId = id
            return this
        }

        fun createLiveData(dao: HistoryDao): LiveData<List<HistoryModel>> {
            if (null != accountId) {
                return dao.getHistoriesBetweenDatesOnlyForAccount(start,end, accountId!!)
            }
            else if (null != groupId) {
                return dao.getHistoriesBetweenDatesOnlyForGroup(start,end, groupId!!)
            }
            else {
                return dao.getHistoriesBetweenDates(start,end)
            }
        }

        override fun toString(): String {
            return "HistoriesLiveDataFactory{start=$start, end=$end, accountId=$accountId, groupId=$groupId}"
        }
    }

    private val historiesDao: HistoryDao
    init {
        val db = ExpensesDatabase.getInstance(app)
        historiesDao = db.historyDao
    }

    private val _historyLiveData = MediatorLiveData<List<HistoryModel>>()
    private val _historySummaryLiveData = MediatorLiveData<HistorySummary>()
    private var _originalHistoryLiveData: LiveData<List<HistoryModel>>? = null
    private val _historyFilterDataLiveData = MutableLiveData<HistoryFilterData>(null)
    private var filterJob: Job? = null
    private var summaryJob: Job? = null

    val historySummary: LiveData<HistorySummary> get() = _historySummaryLiveData

    private fun loadHistories(factory: HistoriesLiveDataFactory) {
        if (_originalHistoryLiveData == null){
            val source = factory.createLiveData(historiesDao)
            _historyLiveData.addSource(source) { handleHistoryLiveDataSourceChanged() }
            _historySummaryLiveData.addSource(source) { handleHistorySummaryLiveDataSourceChanged() }
            _historyLiveData.addSource(_historyFilterDataLiveData) { handleHistoryLiveDataSourceChanged() }
            _originalHistoryLiveData = source
        }
    }

    private fun handleHistoryLiveDataSourceChanged() {
        // cancel running filter job
        filterJob?.cancel()

        val histories = _originalHistoryLiveData?.value
        val filterData = _historyFilterDataLiveData.value

        // if original histories is null or empty then send empty list
        if (histories.isNullOrEmpty()) {
            _historyLiveData.postValue(emptyList())
            return
        }

        // is filter data is null then set original histories
        if (null == filterData) {
            _historyLiveData.postValue(histories)
            return
        }

        filterJob = viewModelScope.launch(Dispatchers.Default) {
            // before start ensure coroutine is active to avoid unnecessary computation
            ensureActive()

            // perform actual filter
            var filteredHistories = filterData.let {  histories.filter{ history -> filterData.match(history) } }

            // before returning result ensure coroutine is active to avoid posting unuseful histories
            ensureActive()

            // post the filtered histories
            _historyLiveData.postValue(filteredHistories)
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

    fun getMonthlyHistories(monthYear: MonthYear): LiveData<List<HistoryModel>> {
        loadHistories(HistoriesLiveDataFactory.forMonthYear(monthYear))
        return _historyLiveData
    }

    fun getMonthlyHistoriesForAccount(monthYear: MonthYear, accountId: Long): LiveData<List<HistoryModel>> {
        loadHistories(HistoriesLiveDataFactory.forMonthYear(monthYear).ofAccount(accountId))
        return _historyLiveData
    }

    fun getMonthlyHistoriesForGroup(monthYear: MonthYear, groupId: Long): LiveData<List<HistoryModel>> {
        loadHistories(HistoriesLiveDataFactory.forMonthYear(monthYear).ofGroup(groupId))
        return _historyLiveData
    }

    fun getDailyHistories(date: Date): LiveData<List<HistoryModel>> {
        loadHistories(HistoriesLiveDataFactory.forDate(date))
        return _historyLiveData
    }

    fun getDailyHistoriesForAccount(date: Date, accountId: Long): LiveData<List<HistoryModel>> {
        loadHistories(HistoriesLiveDataFactory.forDate(date).ofAccount(accountId))
        return _historyLiveData
    }

    fun getDailyHistoriesForGroup(date: Date, groupId: Long): LiveData<List<HistoryModel>> {
        loadHistories(HistoriesLiveDataFactory.forDate(date).ofGroup(groupId))
        return _historyLiveData
    }

    fun applyHistoryFilter(filterData: HistoryFilterData) {
        _historyFilterDataLiveData.value = filterData
    }
}