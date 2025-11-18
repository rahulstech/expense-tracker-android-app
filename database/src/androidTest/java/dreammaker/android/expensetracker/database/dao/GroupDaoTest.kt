package dreammaker.android.expensetracker.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.FAKE_DATA_8
import dreammaker.android.expensetracker.database.FakeDataCallback
import dreammaker.android.expensetracker.database.createInMemoryDB
import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.runOnLiveDataResultReceived
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class GroupDaoTest {

    private var db: ExpensesDatabase? = null
    private var _dao: GroupDao? = null

    private val dao: GroupDao get() = _dao!!

    @Before
    fun setUp() {
        db = createInMemoryDB(ExpensesDatabase::class.java, FakeDataCallback(FAKE_DATA_8))
        _dao = db?.groupDao
    }

    @After
    fun tearDown() {
        _dao = null
        db?.close()
    }

    @Test
    fun insertGroup() {
        val group = GroupEntity(0,"Test Group 1",100f,LocalDateTime.now(),1)
        val id = dao.insertGroup(group)
        assertEquals(3, id)
    }

    @Test
    fun findGroupById() {
        val expected = GroupEntity(2,"Group 2",-200f, LocalDateTime.of(2025,11,1,13,19,56),3)
        val actual = dao.findGroupById(2)
        assertEquals(expected, actual)
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
        val changes = dao.deleteGroup(1)
        assertEquals(1, changes)
    }

    @Test
    fun deleteMultipleGroups() {
        val changes = dao.deleteMultipleGroups(listOf(1, 2))
        assertEquals(2, changes)
    }
}
