package dreammaker.android.expensetracker.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.FAKE_DATA_8
import dreammaker.android.expensetracker.database.FakeDataCallback
import dreammaker.android.expensetracker.database.createInMemoryDB
import dreammaker.android.expensetracker.database.model.AccountIdName
import dreammaker.android.expensetracker.database.model.GroupIdName
import dreammaker.android.expensetracker.database.model.HistoryDetails
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.database.model.HistoryType
import dreammaker.android.expensetracker.database.runOnLiveDataResultReceived
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class HistoryDaoTest {

    var db: ExpensesDatabase? = null
    var _dao: HistoryDao? = null

    val dao: HistoryDao get() = _dao!!

    @Before
    fun setUp() {
        db = createInMemoryDB(ExpensesDatabase::class.java, FakeDataCallback(FAKE_DATA_8))
        _dao = db?.historyDao
    }

    @After
    fun tearDown() {
        _dao = null
        db?.close()
    }

    // -------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------

    @Test
    fun insertHistory() {
        val history = HistoryEntity(
            id = 0,
            type = HistoryType.DEBIT,
            primaryAccountId = 1,
            secondaryAccountId = null,
            groupId = 1,
            amount = 77.0f,
            date = LocalDate.of(2025, 4, 1),
            note = "extra test history"
        )

        val id = dao.insertHistory(history)
        assertEquals(6, id) // existing ids are 1..5
    }

    // -------------------------------------------------------------
    // FIND BY ID
    // -------------------------------------------------------------

    @Test
    fun findHistoryById() {
        val actual = dao.findHistoryById(1)

        val expected = HistoryEntity(
            id = 1,
            type = HistoryType.DEBIT,
            primaryAccountId = 1,
            secondaryAccountId = null,
            groupId = 1,
            amount = 50.0f,
            date = LocalDate.of(2025, 2, 16),
            note = "transaction 1"
        )

        assertEquals(expected, actual)
    }

    @Test
    fun findHistoryByInvalidId() {
        val actual = dao.findHistoryById(999)
        assertEquals(null, actual)
    }

    // -------------------------------------------------------------
    // LIVE: Single History
    // -------------------------------------------------------------

    @Test
    fun getLiveHistoryById() {
        runOnLiveDataResultReceived(dao.getLiveHistoryById(2)) { actual ->

            val history = HistoryEntity(
                id = 2,
                type = HistoryType.CREDIT,
                primaryAccountId = 2,
                secondaryAccountId = null,
                groupId = 1,
                amount = 150f,
                date = LocalDate.of(2025, 2, 26),
                note = "transaction 2"
            )
            val primaryAccount = AccountIdName(2,"Account 2")
            val group = GroupIdName(1,"Group 1")
            val expected = HistoryDetails(
                history = history,
                primaryAccount = primaryAccount,
                secondaryAccount = null,
                group = group,
            )

            assertEquals(expected, actual)
        }
    }

    // -------------------------------------------------------------
    // PAGINATION
    // -------------------------------------------------------------

    @Test
    fun getHistoriesPagination() {
        val list = dao.getHistories(0, 2)

        assertEquals(2, list.size)
        assertEquals(1, list[0].id)
        assertEquals(2, list[1].id)
    }

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------

    @Test
    fun updateHistory() {
        val updated = HistoryEntity(
                id = 2,
                type = HistoryType.CREDIT,
                primaryAccountId = 2,
                secondaryAccountId = null,
                groupId = 1,
                amount = 120f,
                date = LocalDate.of(2025, 2, 24),
                note = "updated note for test"
            )

        val changes = dao.updateHistory(updated)
        assertEquals(1, changes)
    }

    // -------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------

    @Test
    fun deleteHistory() {
        val changes = dao.deleteHistory(4)
        assertEquals(1, changes)
    }

    @Test
    fun deleteMultipleHistories() {
        val changes = dao.deleteMultipleHistories(listOf(1, 2))
        assertEquals(2, changes)
    }
}
