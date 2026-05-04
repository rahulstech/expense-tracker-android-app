package dreammaker.android.expensetracker.database

import dreammaker.android.expensetracker.database.dao.AccountDao
import dreammaker.android.expensetracker.database.dao.AnalyticsDao
import dreammaker.android.expensetracker.database.dao.GroupDao
import dreammaker.android.expensetracker.database.dao.HistoryDao

interface IExpenseDatabase {

    fun <V> runInTransaction(task: suspend ()->V): V

    val historyDao: HistoryDao

    val accountDao: AccountDao

    val groupDao: GroupDao

    val analyticsDao: AnalyticsDao
}
