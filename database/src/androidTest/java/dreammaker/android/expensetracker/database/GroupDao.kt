package dreammaker.android.expensetracker.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroupDaoTest {

    lateinit var db: ExpensesDatabase
    lateinit var dao: GroupDao

    @Before
    fun setUp() {
        db = createInMemoryDB(ExpensesDatabase::class.java,FakeDataCallback(FAKE_DATA_7))
        dao = db.groupDao
    }

    @After
    fun tearDown() {
        runCatching { db.close() }
    }

    @Test
    fun test_getLatestUsedThreeGroups() {
        val liveData = dao.getLatestUsedThreeGroups()
        runOnLiveDataResultReceived(liveData) { groups ->
            val expected = setOf(
                GroupModel(1,"Person 1", 100f),
                GroupModel(2, "Person 2", -120f)
            )

            val actual = HashSet(groups)

            assertEquals("size mismatch", 3, groups.size)
            assertEquals("content mismatch", expected,actual)
        }
    }
}