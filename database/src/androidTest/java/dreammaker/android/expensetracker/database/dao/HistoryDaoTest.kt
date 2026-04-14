package dreammaker.android.expensetracker.database.dao

import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.createInMemoryDB
import dreammaker.android.expensetracker.database.fake.FakeDataCallbacks
import dreammaker.android.expensetracker.database.model.AccountIdName
import dreammaker.android.expensetracker.database.model.GroupIdName
import dreammaker.android.expensetracker.database.model.HistoryDetails
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.database.model.HistoryType
import dreammaker.android.expensetracker.database.runOnLiveDataResultReceived
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class HistoryDaoTest {

    lateinit var db: ExpensesDatabase
    lateinit var dao: HistoryDao


    @Before
    fun setUp() {
        db = createInMemoryDB(FakeDataCallbacks.getCallbackForCurrentDBVersion())
        dao = db.historyDao
    }

    @After
    fun tearDown() {
        db.close()
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

        val actual = dao.findHistoryById(id)
        val expected = history.copy(id=id)

        assertTrue("id must be a positive number", id > 0)
        assertEquals("inserted history not found by id", expected, actual?.history)
    }

    // -------------------------------------------------------------
    // FIND BY ID
    // -------------------------------------------------------------

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
        val list = dao.getHistories(2, 0)

        assertEquals(2, list.size)
        assertEquals(1, list[0].id)
        assertEquals(2, list[1].id)
    }

    @Test
    fun getPagedHistories_returnsCorrectData() = runTest {

        // Raw query
        val query = SimpleSQLiteQuery("SELECT * FROM histories WHERE `groupId` = ? ORDER BY `date` DESC", arrayOf(1))

        val pagingSource = dao.getPagedHistories(query)

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        val data = (result as PagingSource.LoadResult.Page).data

        assertEquals("fetch size mismatch",2, data.size)

        val historyIds = data.map { it.history.id }

        assertEquals("history ids mismatch",listOf<Long>(2,1), historyIds)
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
