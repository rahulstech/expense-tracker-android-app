package dreammaker.android.expensetracker.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.createInMemoryDB
import dreammaker.android.expensetracker.database.fake.FakeDataCallbacks
import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.runOnLiveDataResultReceived
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class AccountDaoTest {

    lateinit var db: ExpensesDatabase
    lateinit var dao: AccountDao


    @Before
    fun setUp() {
        db = createInMemoryDB(FakeDataCallbacks.getCallbackForCurrentDBVersion())
        dao = db.accountDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAccount() {
        val account = AccountEntity(
            0,
            "Test Account 1",
            120f,
            // use a specific value instead of now, otherwise due to nanoseconds mismatch it may fail sometimes
            LocalDateTime.of(2026,1,1,0,0,0,0),
            1)
        val id = dao.insertAccount(account)

        val actual = dao.findAccountById(id)
        val expected = account.copy(id=id)

        assertTrue("id must be positive number", id > 0)
        assertEquals("inserted account not found by id", expected, actual)
    }

    @Test
    fun getFlowAccountById() = runTest {
        val actual = dao.getFlowAccountById(1).first()
        val expected = AccountEntity(1,"Account 1",150.00f, LocalDateTime.of(2025,10,15,15,56,20),1)
        assertEquals(expected,actual)
    }

    @Test
    fun getLiveAllAccounts() {
        runOnLiveDataResultReceived(dao.getLiveAllAccounts()) { actual ->
            val expected = listOf(
                AccountEntity(1,"Account 1",150.00f,LocalDateTime.of(2025,10,15,15,56,20),1),
                AccountEntity(2,"Account 2",2000.00f, null,null)
            )
            assertEquals(expected,actual)
        }
    }

    @Test
    fun getLiveRecentlyUsedAccounts() {
        runOnLiveDataResultReceived(dao.getLiveRecentlyUsedAccounts(3)) { actual ->
            val expected = listOf(
                AccountEntity(1,"Account 1",150.00f,LocalDateTime.of(2025,10,15,15,56,20),1)
            )
            assertEquals(expected,actual)
        }
    }

    @Test
    fun updateAccount() {
        val account = AccountEntity(1,"Account 1",160.00f, LocalDateTime.now(),2)
        val changes = dao.updateAccount(account)
        assertEquals(1,changes)
    }

    @Test
    fun deleteAccount() {
        dao.deleteAccount(1)
        val account = dao.findAccountById(1)
        assertNull(account)
    }

    @Test
    fun deleteMultipleAccounts() {
        dao.deleteMultipleAccounts(listOf(1,2))
        runOnLiveDataResultReceived(dao.getLiveAllAccounts()) { actual ->
            assertTrue(actual.isEmpty())
        }
    }
}