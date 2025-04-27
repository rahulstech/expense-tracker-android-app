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
}

class ViewHistoryViewModel(app: Application): AndroidViewModel(app) {

    private val historiesDao: HistoryDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        historiesDao = db.historyDao
    }

    private lateinit var historiesLiveData: LiveData<List<HistoryModel>>

    fun getMonthlyHistories(monthYear: MonthYear): LiveData<List<HistoryModel>> {
        if (!::historiesLiveData.isInitialized) {
            historiesLiveData = HistoriesLiveDataFactory.forMonthYear(monthYear).createLiveData(historiesDao)
        }
        return historiesLiveData
    }

    fun getDailyHistories(date: Date): LiveData<List<HistoryModel>> {
        if (!::historiesLiveData.isInitialized) {
            historiesLiveData = HistoriesLiveDataFactory.forDate(date).createLiveData(historiesDao)
        }
        return historiesLiveData
    }

    fun getDailyHistoriesForAccount(date: Date, accountId: Long): LiveData<List<HistoryModel>> {
        if (!::historiesLiveData.isInitialized) {
            historiesLiveData = HistoriesLiveDataFactory.forDate(date).ofAccount(accountId).createLiveData(historiesDao)
        }
        return historiesLiveData
    }

    fun getDailyHistoriesForGroup(date: Date, groupId: Long): LiveData<List<HistoryModel>> {
        if (!::historiesLiveData.isInitialized) {
            historiesLiveData = HistoriesLiveDataFactory.forDate(date).ofGroup(groupId).createLiveData(historiesDao)
        }
        return historiesLiveData
    }
}