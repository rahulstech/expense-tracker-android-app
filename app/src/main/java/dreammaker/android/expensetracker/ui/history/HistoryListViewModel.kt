package dreammaker.android.expensetracker.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.Date

class HistoryListViewModel(app: Application) : AndroidViewModel(app) {

    private val historyDao: HistoryDao

    private lateinit var histories: LiveData<List<HistoryModel>>

    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
    }

    fun getHistories(): LiveData<List<HistoryModel>> {
        if (!::histories.isInitialized) {
            histories = historyDao.getHistoriesBetweenDates(
                Date(
                    2025,
                    0,
                    1
                ), Date()
            )
        }
        return histories
    }
}