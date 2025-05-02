package dreammaker.android.expensetracker.database

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
        db = createInMemoryDB(ExpensesDatabase::class.java,FakeDataCallback(FAKE_DATA_7))
        dao = db.historyDao
    }

    @After
    fun tearDown() {
        runCatching { db.close() }
    }

    @Test
    fun testGetHistoriesBetweenDates() {
        val expected = listOf(
            HistoryModel(4, HistoryType.CREDIT, 1, null,2,
                AccountModel(1, "Account 1",null),null,
                GroupModel(2,"Person 2",null),
                120.00f, Date.valueOf("2025-01-20"), "transaction 4"),

            HistoryModel(1, HistoryType.DEBIT, 1, null,1,
                AccountModel(1, "Account 1",null),null,
                GroupModel(1,"Person 1",null),
                50.00f, Date.valueOf("2025-02-16"), "transaction 1"),

            HistoryModel(1, HistoryType.CREDIT,2,null,1,
                AccountModel(2, "Account 2",null), null,
                GroupModel(1,"Person 1",null),
                100.00f, Date.valueOf("2025-02-26"), "transaction 2"),

            HistoryModel(1, HistoryType.TRANSFER,1,2,null,
                AccountModel(2, "Account 2",null), AccountModel(1, "Account 1",null),
                null, 50.00f, Date.valueOf("2025-03-06"), "transfer 1"),
        )

        val results = dao.getHistoriesBetweenDates(Date(2025,0,1), Date(2025,2,31))
        runOnLiveDataResultReceived(results) {
            assertEquals(expected, it)
        }
    }
}