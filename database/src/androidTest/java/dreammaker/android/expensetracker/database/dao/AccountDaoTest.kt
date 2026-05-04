package dreammaker.android.expensetracker.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.createInMemoryDB
import dreammaker.android.expensetracker.database.fake.FakeDataCallbacks
import dreammaker.android.expensetracker.database.model.AccountEntity
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
    fun getFlowAccountById() = runTest {
        val actual = dao.findAccountByIdFlow(1).first()
        val expected = AccountEntity(1,"Account 1",150.0, LocalDateTime.of(2025,10,15,15,56,20),1)
        assertEquals(expected,actual)
    }

    @Test
    fun insert() = runTest {
        val account = AccountEntity(
            0,
            "Test Account Coroutine",
            200.0,
            LocalDateTime.of(2026,2,1,0,0,0,0),
            2)
        val id = dao.insert(account)

        val actual = dao.findAccountByIdFlow(id).first()
        val expected = account.copy(id=id)

        assertTrue("id must be positive number", id > 0)
        assertEquals("inserted account not found by id", expected, actual)
    }

    @Test
    fun insertMultiple() = runTest {
        val accounts = listOf(
            AccountEntity(0, "Multi 1", 100.0, null, null),
            AccountEntity(0, "Multi 2", 200.0, null, null)
        )
        val ids = dao.insertMultiple(accounts)
        assertEquals(2, ids.size)
        
        val all = dao.getAccountsFlow().first()
        // Account 1, Account 2 are already there from FakeData
        assertTrue(all.any { it.name == "Multi 1" })
        assertTrue(all.any { it.name == "Multi 2" })
    }

    @Test
    fun getAccountsFlow() = runTest {
        val actual = dao.getAccountsFlow().first()
        // Fake data has "Account 1" and "Account 2"
        assertEquals(2, actual.size)
        assertEquals("Account 1", actual[0].name)
        assertEquals("Account 2", actual[1].name)
    }

    @Test
    fun update() = runTest {
        val account = AccountEntity(1,"Account 1 Updated",160.0, LocalDateTime.now(),2)
        dao.update(account)
        val actual = dao.findAccountByIdFlow(1).first()
        assertEquals("Account 1 Updated", actual?.name)
    }

    @Test
    fun deleteById() = runTest {
        val changes = dao.deleteById(1)
        assertEquals(1, changes)
        val account = dao.findAccountByIdFlow(1).first()
        assertNull(account)
    }

    @Test
    fun deleteMultipleByIds() = runTest {
        val changes = dao.deleteMultipleByIds(listOf(1,2))
        assertEquals(2, changes)
        val all = dao.getAccountsFlow().first()
        assertTrue(all.isEmpty())
    }
}
