package rahulstech.android.expensetracker.domain.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class GroupRepositoryImplTest {

    lateinit var groupRepo: GroupRepository
    lateinit var cache: FakeLocalCache
    lateinit var db: FakeExpenseDatabase
    lateinit var groupDao: FakeGroupDao

    @Before
    fun setUp() {
        db = FakeExpenseDatabase()
        groupDao = db.groupDao
        cache = FakeLocalCache()
        groupRepo = GroupRepositoryImpl(db,cache)
    }

    @Test
    fun insertGroup() {
        val group = Group("Test Group 1", 120)
        val actual = groupRepo.insertGroup(group)
        assertEquals(3,actual.id)
        assertEquals(actual.lastUsed, groupDao.lastInsertedGroup.lastUsed)
        assertEquals(1,actual.totalUsed)
    }

    @Test
    fun findGroupById() {
        val actual = groupRepo.findGroupById(1L)
        val expected = Group("Group 1", 100f, 1L, LocalDateTime.of(2025,6,11,14,20,19),1)
        assertEquals(expected,actual)
    }

    @Test
    fun updateGroup() {
        val id = 2L
        val group = Group("Updated Group 2", 2000f, id)
        val saved = groupRepo.updateGroup(group)
        assertTrue(saved)
        assertEquals(4L,groupDao.groups[id]!!.totalUsed!!)
    }

    @Test
    fun updateGroup_NotExisting() {
        val id = 5L
        val group = Group("Not Exists", 2000f, id)
        val saved = groupRepo.updateGroup(group)
        assertFalse(saved)
    }

    @Test
    fun creditBalance() {
        val id = 2L
        groupRepo.creditDue(id, 200f)
        assertEquals(2200f,groupDao.groups[id]!!.balance)
    }

    @Test
    fun debitDue() {
        val id = 2L
        groupRepo.debitDue(id, 200f)
        assertEquals(1800f,groupDao.groups[id]!!.balance)
    }

    @Test
    fun deleteGroup() {
        groupRepo.deleteGroup(1L)
        assertNull(groupDao.groups[1L])
    }

    @Test
    fun deleteMultipleAccount() {
        groupRepo.deleteMultipleGroups(listOf(1L,2L))
        assertNull(groupDao.groups[1L])
        assertNull(groupDao.groups[2L])
    }
}