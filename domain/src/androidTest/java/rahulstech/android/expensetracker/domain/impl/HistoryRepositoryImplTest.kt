package rahulstech.android.expensetracker.domain.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class HistoryRepositoryImplTest {

    lateinit var historyRepo: HistoryRepository
    lateinit var db: FakeExpenseDatabase
    lateinit var accountRepo: FakeAccountRepository
    lateinit var groupRepo: FakeGroupRepository

    @Before
    fun setUp() {
        db = FakeExpenseDatabase()
        accountRepo = FakeAccountRepository()
        groupRepo = FakeGroupRepository()
        historyRepo = HistoryRepositoryImpl(db,accountRepo,groupRepo)
    }

    @Test
    fun insertHistory() {
        val history = History.CreditHistory(
            id=0,
            amount=120f,
            date = LocalDate.of(2025,10,10),
            note = "Credit",
            primaryAccountId = 2L,
            groupId = 1L
        )

        historyRepo.insertHistory(history)

        assertEquals("update balance",2120f, accountRepo.accounts[2L]!!.balance)
        assertEquals("update due",-20f, groupRepo.groups[1L]!!.balance)
    }

    @Test
    fun updateHistory() {
        val history = History.CreditHistory(
            id=1,
            amount=50f,
            date = LocalDate.of(2025,10,10),
            note = "Credit",
            primaryAccountId = 2L,
            groupId = 1L
        )

        val updated = historyRepo.updateHistory(history)

        assertTrue(updated)

        assertEquals("reset balance", 200f, accountRepo.accounts[1L]!!.balance)
        assertEquals("reset due", 1900f, groupRepo.groups[2L]!!.balance)

        assertEquals("update balance",2050f, accountRepo.accounts[2L]!!.balance)
        assertEquals("update due", 50f, groupRepo.groups[1L]!!.balance)
    }
}