package dreammaker.android.expensetracker.database

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dreammaker.android.expensetracker.database.dao.AccountDao
import dreammaker.android.expensetracker.database.dao.GroupDao
import dreammaker.android.expensetracker.database.dao.HistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpenseDatabaseHiltModule {

    @Provides
    @Singleton
    fun expenseDB(@ApplicationContext context: Context): IExpenseDatabase =
        ExpensesDatabase.getInstance(context)

    @Provides
    fun accountDao(db: IExpenseDatabase): AccountDao =
        db.accountDao

    @Provides
    fun groupDao(db: IExpenseDatabase): GroupDao =
        db.groupDao

    @Provides
    fun historyDao(db: IExpenseDatabase): HistoryDao =
        db.historyDao
}