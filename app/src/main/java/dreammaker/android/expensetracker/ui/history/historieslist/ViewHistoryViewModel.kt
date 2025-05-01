package dreammaker.android.expensetracker.ui.history.historieslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.util.MonthYear

class HistoriesLiveDataFactory private constructor(val start: Date, val end: Date){

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

class ViewHistoryViewModel(app: Application): AndroidViewModel(app) {

    private val TAG = ViewHistoryViewModel::class.simpleName

    private val historiesDao: HistoryDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        historiesDao = db.historyDao
    }

    private lateinit var historiesLiveData: LiveData<List<HistoryModel>>

    private fun getOrCreateHistoriesLiveData(factory: HistoriesLiveDataFactory): LiveData<List<HistoryModel>> {
        if (!::historiesLiveData.isInitialized) {
            historiesLiveData = factory.createLiveData(historiesDao)
        }
        return historiesLiveData
    }

    fun getHistories(): List<HistoryModel>? {
        if (!::historiesLiveData.isInitialized) {
            return null
        }
        return historiesLiveData.value
    }

    fun getMonthlyHistories(monthYear: MonthYear): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forMonthYear(monthYear))

    fun getMonthlyHistoriesForAccount(monthYear: MonthYear, accountId: Long): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forMonthYear(monthYear).ofAccount(accountId))

    fun getMonthlyHistoriesForGroup(monthYear: MonthYear, groupId: Long): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forMonthYear(monthYear).ofGroup(groupId))

    fun getDailyHistories(date: Date): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forDate(date))

    fun getDailyHistoriesForAccount(date: Date, accountId: Long): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forDate(date).ofAccount(accountId))

    fun getDailyHistoriesForGroup(date: Date, groupId: Long): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forDate(date).ofGroup(groupId))
}