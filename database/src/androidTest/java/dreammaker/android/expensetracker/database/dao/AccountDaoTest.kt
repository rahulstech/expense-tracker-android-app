package dreammaker.android.expensetracker.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.FAKE_DATA_8
import dreammaker.android.expensetracker.database.FakeDataCallback
import dreammaker.android.expensetracker.database.createInMemoryDB
import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.runOnLiveDataResultReceived
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class AccountDaoTest {

    var db: ExpensesDatabase? = null
    var _dao: AccountDao? = null

    val dao: AccountDao get() = _dao!!

    @Before
    fun setUp() {
        db = createInMemoryDB(ExpensesDatabase::class.java, FakeDataCallback(FAKE_DATA_8))
        _dao = db?.accountDao
    }

    @After
    fun tearDown() {
        _dao = null
        db?.close()
    }

    @Test
    fun insertAccount() {
        val account = AccountEntity(0,"Test Account 1", 120f, LocalDateTime.now(), 1)
        val id = dao.insertAccount(account)
        assertEquals(3,id)
    }

    @Test
    fun findAccountById() {
        val expected = AccountEntity(1,"Account 1",150.00f, LocalDateTime.of(2025,10,15,15,56,20),1)
        val actual = dao.findAccountById(1)
        assertEquals(expected,actual)
    }

    @Test
    fun getLiveAccountById() {
        runOnLiveDataResultReceived(dao.getLiveAccountById(1)) { actual ->
            val expected = AccountEntity(1,"Account 1",150.00f, LocalDateTime.of(2025,10,15,15,56,20),1)
            assertEquals(expected,actual)
        }
    }

    @Test
    fun getLiveAllAccounts() {
        runOnLiveDataResultReceived(dao.getLiveAllAccounts()) { actual ->
            val expected = listOf<AccountEntity>(
                AccountEntity(1,"Account 1",150.00f,LocalDateTime.of(2025,10,15,15,56,20),1),
                AccountEntity(2,"Account 2",2000.00f, null,null)
            )
            assertEquals(expected,actual)
        }
    }

    @Test
    fun getLiveRecentlyUsedAccounts() {
        runOnLiveDataResultReceived(dao.getLiveRecentlyUsedAccounts(3)) { actual ->
            val expected = listOf<AccountEntity>(
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
        val changes = dao.deleteAccount(1)
        assertEquals(1,changes)
    }

    @Test
    fun deleteMultipleAccounts() {
        val changes = dao.deleteMultipleAccounts(listOf(1,2))
        assertEquals(2,changes)
    }
}