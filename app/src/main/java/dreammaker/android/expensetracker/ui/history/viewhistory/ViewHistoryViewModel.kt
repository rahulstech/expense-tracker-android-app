package dreammaker.android.expensetracker.ui.history.viewhistory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.HistoryModel

class ViewHistoryViewModel(app: Application): AndroidViewModel(app) {

    private val historiesDao: HistoryDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        historiesDao = db.historyDao
    }

    private lateinit var monthlyHistories: LiveData<List<HistoryModel>>

    fun getMonthlyHistories(month: Int, year: Int): LiveData<List<HistoryModel>> {
        if (!::monthlyHistories.isInitialized) {
            val start = Date(year, month, 1)
            val end = start.lastDateOfThisMonth()
            monthlyHistories = historiesDao.getHistoriesBetweenDates(start,end)
        }
        return monthlyHistories
    }

    private lateinit var dailyHistories: LiveData<List<HistoryModel>>

    fun getDailyHistories(date: Date): LiveData<List<HistoryModel>> {
        if (!::dailyHistories.isInitialized) {
            dailyHistories = historiesDao.getHistoriesForDate(date)
        }
        return dailyHistories
    }
}