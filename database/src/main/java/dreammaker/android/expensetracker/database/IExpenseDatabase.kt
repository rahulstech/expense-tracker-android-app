package dreammaker.android.expensetracker.database

import dreammaker.android.expensetracker.database.dao.AccountDao
import dreammaker.android.expensetracker.database.dao.GroupDao
import dreammaker.android.expensetracker.database.dao.HistoryDao
import java.util.concurrent.Callable

interface IExpenseDatabase {

    fun runInTransaction(task: Runnable)

    fun <V> runInTransaction(task: Callable<V>): V

    val historyDao: HistoryDao

    val accountDao: AccountDao

    val groupDao: GroupDao
}
