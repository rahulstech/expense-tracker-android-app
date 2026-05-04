package dreammaker.android.expensetracker.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.createInMemoryDB
import dreammaker.android.expensetracker.database.fake.FakeDataCallbacks
import dreammaker.android.expensetracker.database.model.GroupEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class GroupDaoTest {

    lateinit var db: ExpensesDatabase
    lateinit var dao: GroupDao

    @Before
    fun setUp() {
        db = createInMemoryDB(FakeDataCallbacks.getCallbackForCurrentDBVersion())
        dao = db.groupDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ----------------------------------------------------
    //      Kotlin Coroutine and Flow based DAO tests
    // ----------------------------------------------------

    @Test
    fun insert() = runTest {
        val group = GroupEntity(0, "New Group", 50.0, null, null)
        val id = dao.insert(group)
        assertTrue(id > 0)
        val actual = dao.findByIdFlow(id).first()
        assertEquals("New Group", actual?.name)
    }

    @Test
    fun insertMultiple() = runTest {
        val groups = listOf(
            GroupEntity(0, "G1", 10.0, null, null),
            GroupEntity(0, "G2", 20.0, null, null)
        )
        dao.insertMultiple(groups)
        val all = dao.getAllGroupsFlow().first()
        assertTrue(all.any { it.name == "G1" })
        assertTrue(all.any { it.name == "G2" })
    }

    @Test
    fun getAllGroupsFlow() = runTest {
        val groups = dao.getAllGroupsFlow().first()
        // Fake data has 2 groups
        assertEquals(2, groups.size)
    }

    @Test
    fun findByIdFlow() = runTest {
        val group = dao.findByIdFlow(1).first()
        assertEquals("Group 1", group?.name)
    }

    @Test
    fun getRecentlyUsedGroupsFlow() = runTest {
        val groups = dao.getRecentlyUsedGroupsFlow(3).first()
        // Only Group 2 has lastUsed in fake data
        assertEquals(1, groups.size)
        assertEquals("Group 2", groups[0].name)
    }

    @Test
    fun update() = runTest {
        val group = GroupEntity(1, "Group 1 Updated", 150.0, null, null)
        dao.update(group)
        val actual = dao.findByIdFlow(1).first()
        assertEquals("Group 1 Updated", actual?.name)
    }

    @Test
    fun delete() = runTest {
        val changes = dao.delete(1)
        assertEquals(1, changes)
        val actual = dao.findByIdFlow(1).first()
        assertNull(actual)
    }

    @Test
    fun deleteMultiple() = runTest {
        val changes = dao.deleteMultiple(listOf(1, 2))
        assertEquals(2, changes)
        val all = dao.getAllGroupsFlow().first()
        assertTrue(all.isEmpty())
    }
}
