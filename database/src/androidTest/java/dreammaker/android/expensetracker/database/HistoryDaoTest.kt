package dreammaker.android.expensetracker.database

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryDaoTest {

    lateinit var db: ExpensesDatabase
    lateinit var dao: HistoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(context, ExpensesDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    val wrapper = SQLiteStatementExecutorWrapperBuilder().buildFromSupportSQLiteDatabase(db)
                    FAKE_DATA_7.addFakeData(wrapper)
                }
            })
            .build()
        dao = db.historyDao
    }

    @After
    fun tearDown() {
        runCatching { db.close() }
    }

    @Test
    fun testGetHistoriesBetweenDates() {
        val expected = listOf(
            HistoryModel(1, HistoryType.DEBIT, 1, null, null,1,
                AccountModel(1, "Account 1",null),null,
                null, PersonModel(1,"Person 1",null),
                50.00f, Date.valueOf("2025-02-16"), "transaction 1"),

            HistoryModel(1, HistoryType.CREDIT,null,2,1,null,
                null, AccountModel(2, "Account 2",null),
                PersonModel(1,"Person 1",null), null,
                100.00f, Date.valueOf("2025-02-26"), "transaction 2"),

            HistoryModel(1, HistoryType.TRANSFER,1,2,null,null,
                AccountModel(2, "Account 2",null), AccountModel(1, "Account 1",null),
                null,null, 50.00f, Date.valueOf("2025-03-06"), "transfer 1"),
        )

        val results = dao.getHistoriesBetweenDates(Date(2025,0,1), Date(2025,2,31))
        runOnLiveDataResultReceived(results) {
            assertEquals(expected, it)
        }
    }
}