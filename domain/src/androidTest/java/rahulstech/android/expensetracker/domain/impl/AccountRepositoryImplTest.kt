package rahulstech.android.expensetracker.domain.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rahulstech.android.expensetracker.domain.AccountRepository
import rahulstech.android.expensetracker.domain.model.Account
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class AccountRepositoryImplTest {

    lateinit var accountRepo: AccountRepository
    lateinit var cache: FakeLocalCache
    lateinit var db: FakeExpenseDatabase
    lateinit var accountDao: FakeAccountDao

    @Before
    fun setUp() {
        db = FakeExpenseDatabase()
        accountDao = db.accountDao
        cache = FakeLocalCache()
        accountRepo = AccountRepositoryImpl(db,cache)
    }

    @Test
    fun insertAccount() {
        val account = Account("Test Account 1", 120f)
        val actual = accountRepo.insertAccount(account)
        assertEquals(3,actual.id)
        assertEquals(actual.lastUsed, accountDao.lastInsertedAccount.lastUsed)
        assertEquals(1,actual.totalUsed)
    }

    @Test
    fun findAccountById() {
        val actual = accountRepo.findAccountById(1L)
        val expected = Account("Account 1", 100f, 1L, LocalDateTime.of(2025,6,11,14,20,19),1)
        assertEquals(expected,actual)
    }

    @Test
    fun updateAccount() {
        val id = 2L
        val account = Account("Updated Account 2", 2000f, id)
        val saved = accountRepo.updateAccount(account)
        assertTrue(saved)
        assertEquals(4L,accountDao.accounts[id]!!.totalUsed!!)
    }

    @Test
    fun updateAccount_NotExisting() {
        val id = 5L
        val account = Account("Not Exists", 2000f, id)
        val saved = accountRepo.updateAccount(account)
        assertFalse(saved)
    }

    @Test
    fun creditBalance() {
        val id = 2L
        accountRepo.creditBalance(id, 200f)
        assertEquals(2200f,accountDao.accounts[id]!!.balance)
    }

    @Test
    fun debitBalance() {
        val id = 2L
        accountRepo.debitBalance(id, 200f)
        assertEquals(1800f,accountDao.accounts[id]!!.balance)
    }

    @Test
    fun deleteAccount() {
        accountRepo.deleteAccount(1L)
        assertNull(accountDao.accounts[1L])
    }

    @Test
    fun deleteMultipleAccount() {
        accountRepo.deleteMultipleAccounts(listOf(1L,2L))
        assertNull(accountDao.accounts[1L])
        assertNull(accountDao.accounts[2L])
    }
}