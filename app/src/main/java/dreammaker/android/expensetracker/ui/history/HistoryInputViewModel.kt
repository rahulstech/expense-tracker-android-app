package dreammaker.android.expensetracker.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.History
import dreammaker.android.expensetracker.database.HistoryDao

class HistoryInputViewModel(app: Application) : AndroidViewModel(app) {

    private lateinit var historyDao: HistoryDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        historyDao = db.historyDao
    }

    fun addHistory(history: History) {
        val id = historyDao.insertHistory(history)
    }
}