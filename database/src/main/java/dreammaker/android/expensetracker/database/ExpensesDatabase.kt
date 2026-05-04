package dreammaker.android.expensetracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import dreammaker.android.expensetracker.database.dao.AccountDao
import dreammaker.android.expensetracker.database.dao.AnalyticsDao
import dreammaker.android.expensetracker.database.dao.GroupDao
import dreammaker.android.expensetracker.database.dao.HistoryDao
import dreammaker.android.expensetracker.database.migration.Migration7To8
import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.HistoryEntity
import kotlinx.coroutines.runBlocking

@Database(
    entities = [AccountEntity::class, GroupEntity::class, HistoryEntity::class],
    version = ExpensesDatabase.DB_VERSION
)
@TypeConverters(Converter::class)
abstract class ExpensesDatabase : RoomDatabase(), IExpenseDatabase {

    override fun <V> runInTransaction(task: suspend () -> V): V {
        return runBlocking {
            withTransaction {
                task()
            }
        }
    }

    abstract override val historyDao: HistoryDao

    abstract override val accountDao: AccountDao

    abstract override val groupDao: GroupDao

    abstract override val analyticsDao: AnalyticsDao

    companion object {

        const val DB_NAME = "expenses.db3"
        const val DB_VERSION = 8

        @Volatile
        private var INSTANCE: ExpensesDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): ExpensesDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ExpensesDatabase::class.java,
                    DB_NAME
                ).addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.setForeignKeyConstraintsEnabled(true)
                    }
                }).addMigrations(
                    Migrations.MIGRATION_6_7,
                    Migration7To8()
                ).build().also { INSTANCE = it }
            }
        }
    }
}
