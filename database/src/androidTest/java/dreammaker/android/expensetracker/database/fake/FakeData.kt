package dreammaker.android.expensetracker.database.fake

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.SQLiteStatementExecutorWrapper
import dreammaker.android.expensetracker.database.SQLiteStatementExecutorWrapperBuilder

interface FakeData {
    fun addFakeData(db: SQLiteStatementExecutorWrapper)
}

class FakeDataCallback(private val source: FakeData): RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        val wrapper = SQLiteStatementExecutorWrapperBuilder().buildFromSupportSQLiteDatabase(db)
        source.addFakeData(wrapper)
    }
}

object FakeDataCallbacks {

    fun getCallbackForCurrentDBVersion(): FakeDataCallback =
        getCallbackForDBVersion(ExpensesDatabase.DB_VERSION)!!

    fun getCallbackForDBVersion(version: Int): FakeDataCallback? {
        val fakeData = when(version) {
            6 -> FakeData6()
            7 -> FakeData7()
            8 -> FakeData8()
            else -> null
        }
        return fakeData?.let {
            FakeDataCallback(it)
        }
    }
}

