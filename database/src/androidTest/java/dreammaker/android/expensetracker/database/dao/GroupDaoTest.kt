package dreammaker.android.expensetracker.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.createInMemoryDB
import dreammaker.android.expensetracker.database.fake.FakeDataCallbacks
import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.runOnLiveDataResultReceived
import junit.framework.TestCase.assertEquals
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

    @Test
    fun insertGroup() {
        val group = GroupEntity(
            0,
            "Test Group 1",
            100f,
            // Use specific value instead of LocalDateTime.now(), it may sometime cause nano seconds mismatch an test may fail
            LocalDateTime.of(2026,1,1,0,0,0,0),
            1
        )
        val id = dao.insertGroup(group)
        val expected = dao.findGroupById(id)
        val actual = group.copy(id)

        assertTrue("id must be positive number", id > 0)
        assertEquals("inserted group not found by id", expected, actual)
    }

    @Test
    fun getLiveGroupById() {
        runOnLiveDataResultReceived(dao.getLiveGroupById(2)) { actual ->
            val expected = GroupEntity(2,"Group 2",-200f, LocalDateTime.of(2025,11,1,13,19,56),3)
            assertEquals(expected, actual)
        }
    }

    @Test
    fun getLiveAllGroups() {
        runOnLiveDataResultReceived(dao.getLiveAllGroups()) { actual ->
            val expected = listOf(
                GroupEntity(1,"Group 1",100f,null,null),
                GroupEntity(2,"Group 2",-200f,LocalDateTime.of(2025,11,1,13,19,56),3)
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun getLiveRecentlyUsedGroups() {
        runOnLiveDataResultReceived(dao.getLiveRecentlyUsedGroups(3)) { actual ->
            val expected = listOf(
                GroupEntity(2,"Group 2",-200f,LocalDateTime.of(2025,11,1,13,19,56),3)
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun updateGroup() {
        val group = GroupEntity(1,"Group 1",600f,LocalDateTime.now(),2)
        val changes = dao.updateGroup(group)
        assertEquals(1, changes)
    }

    @Test
    fun deleteGroup() {
        dao.deleteGroup(1)
        val actual =  dao.findGroupById(1)
        assertNull(actual)
    }

    @Test
    fun deleteMultipleGroups() {
        dao.deleteMultipleGroups(listOf(1, 2))
        runOnLiveDataResultReceived(dao.getLiveAllGroups()) { actual ->
            assertTrue(actual.isEmpty())
        }
    }
}
