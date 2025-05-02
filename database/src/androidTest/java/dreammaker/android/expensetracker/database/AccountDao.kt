package dreammaker.android.expensetracker.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountDaoTest {

    lateinit var db: ExpensesDatabase
    lateinit var dao: AccountDao

    @Before
    fun setUp() {
        db = createInMemoryDB(ExpensesDatabase::class.java,FakeDataCallback(FAKE_DATA_7))
        dao = db.accountDao
    }

    @After
    fun tearDown() {
        runCatching { db.close() }
    }

    @Test
    fun test_getLatestUsedThreeAccounts() {
        val liveData = dao.getLatestUsedThreeAccounts()
        runOnLiveDataResultReceived(liveData) { accounts ->
            val expected = setOf(
                AccountModel(1,"Account 1", 150f),
                AccountModel(2, "Account 2", 2000f)
            )

            val actual = HashSet(accounts)

            assertEquals("size mismatch", 3, accounts.size)
            assertEquals("content mismatch", expected,actual)
        }
    }
}