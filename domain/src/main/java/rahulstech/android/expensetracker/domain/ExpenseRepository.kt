package rahulstech.android.expensetracker.domain

import android.content.Context
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.IExpenseDatabase
import rahulstech.android.expensetracker.domain.impl.AccountRepositoryImpl
import rahulstech.android.expensetracker.domain.impl.GroupRepositoryImpl
import rahulstech.android.expensetracker.domain.impl.HistoryRepositoryImpl
import rahulstech.android.expensetracker.domain.impl.LocalCacheImpl

class ExpenseRepository private constructor(private val appContext: Context) {

    companion object {
        private var instance: ExpenseRepository? = null
        private val lock: Any = Any()

        fun getInstance(context: Context): ExpenseRepository = instance ?: synchronized(lock) {
            instance = ExpenseRepository(context.applicationContext)
            instance!!
        }
    }

    private val db: IExpenseDatabase by lazy { ExpensesDatabase.getInstance(appContext) }

    private val cache: LocalCache by lazy { LocalCacheImpl(appContext) }

    val accountRepository: AccountRepository by lazy { AccountRepositoryImpl(db,cache) }

    val groupRepository: GroupRepository by lazy { GroupRepositoryImpl(db,cache) }

    val historyRepository: HistoryRepository by lazy { HistoryRepositoryImpl(db,accountRepository,groupRepository) }
}