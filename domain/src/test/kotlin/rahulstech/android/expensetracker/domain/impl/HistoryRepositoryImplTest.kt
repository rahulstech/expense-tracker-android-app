package rahulstech.android.expensetracker.domain.impl

import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.AccountDao
import dreammaker.android.expensetracker.database.dao.GroupDao
import dreammaker.android.expensetracker.database.dao.HistoryDao
import dreammaker.android.expensetracker.database.model.HistoryDetails
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.database.model.HistoryType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.fake.AccountRepositoryFakeImpl
import rahulstech.android.expensetracker.domain.fake.GroupRepositoryFakeImpl
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate
import java.util.concurrent.Callable

class HistoryRepositoryImplTest {

    lateinit var accountRepo: AccountRepositoryFakeImpl
    lateinit var groupRepo: GroupRepositoryFakeImpl

    lateinit var repo: HistoryRepository

    val expenseDB = mockk<IExpenseDatabase>(relaxed = true, relaxUnitFun = true)

    val mockHistoryDao = mockk<HistoryDao>()
    val mockAccountDao = mockk<AccountDao>()

    val mockGroupDao = mockk<GroupDao>()

    init {
        every { expenseDB.historyDao } returns mockHistoryDao
        every { expenseDB.groupDao } returns mockGroupDao
        every { expenseDB.accountDao } returns mockAccountDao

        // HistoryRepoImpl call runInTransaction of IExpenseDatabase inside insertHistory, updateHistory, deleteHistory;
        // if it is not invoked, whole test will fail
        every { expenseDB.runInTransaction<Any>(any()) } answers {
            firstArg<Callable<Any>>().call()
        }
    }

    @Before
    fun setUp() {
        accountRepo = AccountRepositoryFakeImpl()
        groupRepo = GroupRepositoryFakeImpl()
        repo = HistoryRepositoryImpl(
            db = expenseDB,
            accountRepository = accountRepo,
            groupRepository = groupRepo
        )
    }

    ///////////////////////////////////////////////////////
    ///                 Insert History                 ///
    /////////////////////////////////////////////////////

    @Test
    fun insertCreditHistory() {

        val accountId = 1L
        val groupId = 2L
        val amount = 100f
        val fakeAccounts = accountRepo.accounts
        val fakeGroups = groupRepo.groups
        val oldAccountBalance = fakeAccounts[accountId]!!.balance
        val oldGroupBalance = fakeGroups[groupId]!!.balance

        val history = History.CreditHistory(
            id = 0,
            date = LocalDate.of(2026, 1, 1),
            amount = 100f,
            primaryAccountId = accountId,
            groupId = groupId,
            note = null,
        )

        stubInsert()

        repo.insertHistory(history)

        val newAccountBalance = fakeAccounts[accountId]!!.balance
        val newGroupBalance = fakeGroups[groupId]!!.balance
        val expectedAccountBalance = oldAccountBalance + amount
        val expectedGroupBalance = oldGroupBalance - amount

        verify { mockHistoryDao.insertHistory(any()) }

        assertEquals(expectedAccountBalance, newAccountBalance)
        assertEquals(expectedGroupBalance, newGroupBalance)
    }

    @Test
    fun insertDebitHistory() {

        val accountId = 1L
        val groupId = 2L
        val amount = 100f
        val fakeAccounts = accountRepo.accounts
        val fakeGroups = groupRepo.groups
        val oldAccountBalance = fakeAccounts[accountId]!!.balance
        val oldGroupBalance = fakeGroups[groupId]!!.balance

        val history = History.DebitHistory(
            id = 0,
            date = LocalDate.of(2026, 1, 1),
            amount = 100f,
            primaryAccountId = accountId,
            groupId = groupId,
            note = null,
        )

        stubInsert()

        repo.insertHistory(history)

        val newAccountBalance = fakeAccounts[accountId]!!.balance
        val newGroupBalance = fakeGroups[groupId]!!.balance
        val expectedAccountBalance = oldAccountBalance - amount
        val expectedGroupBalance = oldGroupBalance + amount

        verify { mockHistoryDao.insertHistory(any()) }

        assertEquals(expectedAccountBalance, newAccountBalance)
        assertEquals(expectedGroupBalance, newGroupBalance)
    }

    @Test
    fun insertTransferHistory() {

        val srcAccountId = 1L

        val destAccountId = 2L

        val amount = 100f

        val fakeAccounts = accountRepo.accounts

        val srcOldBalance = fakeAccounts[srcAccountId]!!.balance

        val destOldBalance = fakeAccounts[destAccountId]!!.balance

        val history = History.TransferHistory(
            id = 0,
            date = LocalDate.of(2026, 1, 1),
            amount = 100f,
            primaryAccountId = srcAccountId,
            secondaryAccountId = destAccountId
        )

        stubInsert()

        repo.insertHistory(history)

        val srcNewBalance = fakeAccounts[srcAccountId]!!.balance
        val destNewBalance = fakeAccounts[destAccountId]!!.balance
        val srcExpectedBalance = srcOldBalance - amount
        val destExpectedBalance = destOldBalance + amount

        verify { mockHistoryDao.insertHistory(any()) }
        assertEquals(srcExpectedBalance, srcNewBalance)
        assertEquals(destExpectedBalance, destNewBalance)
    }

    ///////////////////////////////////////////////////////
    ///                 Update History                 ///
    /////////////////////////////////////////////////////

    @Test
    fun updateCreditHistory_amount() {

        val accountId = 1L
        val groupId = 2L
        val oldAmount = 100f
        val newAmount = 370f

        val fakeAccounts = accountRepo.accounts
        val fakeGroups = groupRepo.groups
        val oldAccountBalance = fakeAccounts[accountId]!!.balance
        val oldGroupBalance = fakeGroups[groupId]!!.balance

        val history = History.CreditHistory(
            id = 1,
            date = LocalDate.of(2026, 1, 1),
            amount = newAmount,
            primaryAccountId = accountId,
            groupId = groupId
        )

        stubOldHistory(HistoryEntity(
                id = 1,
                type = HistoryType.CREDIT,
                primaryAccountId = accountId,
                secondaryAccountId = null,
                groupId = groupId,
                amount = oldAmount,
                date = LocalDate.of(2026, 1, 1),
                note = null,
            )
        )

        stubUpdate()

        repo.updateHistory(history)

        val newAccountBalance = fakeAccounts[accountId]!!.balance
        val newGroupBalance = fakeGroups[groupId]!!.balance
        val expectedAccountBalance = oldAccountBalance - oldAmount + newAmount
        val expectedGroupBalance = oldGroupBalance + oldAmount - newAmount

        verify { mockHistoryDao.updateHistory(any()) }
        assertEquals(expectedAccountBalance, newAccountBalance)
        assertEquals(expectedGroupBalance, newGroupBalance)
    }

    @Test
    fun updateDebitHistory_amount() {

        val accountId = 1L
        val groupId = 2L
        val oldAmount = 100f
        val newAmount = 370f

        val fakeAccounts = accountRepo.accounts
        val fakeGroups = groupRepo.groups
        val oldAccountBalance = fakeAccounts[accountId]!!.balance
        val oldGroupBalance = fakeGroups[groupId]!!.balance

        val history = History.DebitHistory(
            id = 1,
            date = LocalDate.of(2026, 1, 1),
            amount = newAmount,
            primaryAccountId = accountId,
            groupId = groupId
        )

        stubOldHistory(HistoryEntity(
                id = 1,
                type = HistoryType.DEBIT,
                primaryAccountId = accountId,
                secondaryAccountId = null,
                groupId = groupId,
                amount = oldAmount,
                date = LocalDate.of(2026, 1, 1),
                note = null,
            )
        )

        stubUpdate()

        repo.updateHistory(history)

        val newAccountBalance = fakeAccounts[accountId]!!.balance
        val newGroupBalance = fakeGroups[groupId]!!.balance
        val expectedAccountBalance = oldAccountBalance + oldAmount - newAmount
        val expectedGroupBalance = oldGroupBalance - oldAmount + newAmount

        verify { mockHistoryDao.updateHistory(any()) }
        assertEquals(expectedAccountBalance, newAccountBalance)
        assertEquals(expectedGroupBalance, newGroupBalance)
    }

    @Test
    fun updateTransferHistory() {

        val srcAccountId = 1L

        val destAccountId = 2L

        val oldAmount = 100f

        val newAmount = 300f

        val fakeAccounts = accountRepo.accounts

        val srcOldBalance = fakeAccounts[srcAccountId]!!.balance

        val destOldBalance = fakeAccounts[destAccountId]!!.balance

        val history = History.TransferHistory(
            id = 1,
            date = LocalDate.of(2026, 1, 1),
            amount = newAmount,
            primaryAccountId = srcAccountId,
            secondaryAccountId = destAccountId
        )

        stubOldHistory( HistoryEntity(
                id = 1,
                type = HistoryType.TRANSFER,
                primaryAccountId = srcAccountId,
                secondaryAccountId = destAccountId,
                groupId = null,
                amount = oldAmount,
                date = LocalDate.of(2026, 1, 1),
                note = null,
            )
        )

        stubUpdate()

        repo.updateHistory(history)

        val srcNewBalance = fakeAccounts[srcAccountId]!!.balance
        val destNewBalance = fakeAccounts[destAccountId]!!.balance
        val srcExpectedBalance = srcOldBalance + oldAmount - newAmount
        val destExpectedBalance = destOldBalance - oldAmount + newAmount

        verify { mockHistoryDao.updateHistory(any()) }
        assertEquals(srcExpectedBalance, srcNewBalance)
        assertEquals(destExpectedBalance, destNewBalance)
    }


    ///////////////////////////////////////////////////////
    ///                    Helpers                     ///
    /////////////////////////////////////////////////////

    private fun stubInsert(id: Long = 1L) {
        every { mockHistoryDao.insertHistory(any()) } returns id
    }

    private fun stubUpdate(success: Int = 1) {
        every { mockHistoryDao.updateHistory(any()) } returns success
    }

    private fun stubOldHistory(entity: HistoryEntity) {
        every { mockHistoryDao.findHistoryById(entity.id) } returns HistoryDetails(
            history = entity,
            primaryAccount = null,
            secondaryAccount = null,
            group = null
        )
    }
}