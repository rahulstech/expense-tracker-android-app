package dreammaker.android.expensetracker.database.dao

import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.createInMemoryDB
import dreammaker.android.expensetracker.database.fake.FakeDataCallbacks
import dreammaker.android.expensetracker.database.model.HistoryEntity
import dreammaker.android.expensetracker.database.model.HistoryType
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
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
    // PAGINATION
    // -------------------------------------------------------------

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
    // Coroutine and Flow based DAO tests
    // -------------------------------------------------------------

    @Test
    fun insert() = runTest {
        val history = HistoryEntity(
            id = 0,
            type = HistoryType.DEBIT,
            primaryAccountId = 1,
            secondaryAccountId = null,
            groupId = 1,
            amount = 100.0,
            date = LocalDate.of(2025, 5, 1),
            note = "coroutine insert"
        )
        val id = dao.insert(history)
        assertTrue(id > 0)
        val actual = dao.findHistoryDetailsByIdFlow(id).first()
        assertEquals(100.0, actual?.history?.amount)
    }

    @Test
    fun insertMultiple() = runTest {
        val histories = listOf(
            HistoryEntity(0, HistoryType.DEBIT, 1, null, 1, 10.0, LocalDate.now(), ""),
            HistoryEntity(0, HistoryType.CREDIT, 1, null, 1, 20.0, LocalDate.now(), "")
        )
        dao.insertMultiple(histories)
    }

    @Test
    fun findHistoryDetailsByIdFlow() = runTest {
        val actual = dao.findHistoryDetailsByIdFlow(1).first()
        assertEquals(1L, actual?.history?.id)
    }

    @Test
    fun getHistories() = runTest {
        val actual = dao.getHistories(2, 0)
        assertEquals(2, actual.size)
    }

    @Test
    fun update() = runTest {
        val history = dao.getHistories(1, 0)[0]
        val updated = history.copy(note = "updated note")
        dao.update(updated)
        val actual = dao.findHistoryDetailsByIdFlow(history.id).first()
        assertEquals("updated note", actual?.history?.note)
    }

    @Test
    fun delete() = runTest {
        val changes = dao.delete(1)
        assertEquals(1, changes)
        val actual = dao.findHistoryDetailsByIdFlow(1).first()
        assertNull(actual)
    }

    @Test
    fun deleteMultiple() = runTest {
        val changes = dao.deleteMultiple(listOf(1, 2))
        assertEquals(2, changes)
    }
}
