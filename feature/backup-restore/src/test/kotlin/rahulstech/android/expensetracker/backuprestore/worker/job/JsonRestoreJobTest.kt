package rahulstech.android.expensetracker.backuprestore.work.restore.job

import dreammaker.android.expensetracker.database.model.HistoryType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import rahulstech.android.expensetracker.backuprestore.asInputStream
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.worker.job.JsonRestoreJob
import java.io.InputStream
import java.time.LocalDate

data class SimpleValue(
    val data: Int
)

class JsonRestoreJobTestImpl(source: InputStream): JsonRestoreJob(1,source) {

    var key1: SimpleValue? = null

    override fun doRestore() {
        while (hasNext()) {
            val name = readNextName()
            when(name) {
                "key1" -> {
                    key1 = readNextObject(SimpleValue::class.java)
                }
                else -> skipNext()
            }
        }
    }
}

class InputStreamWrapper: InputStream() {

    private var org: InputStream? = null

    fun wrap(org: InputStream) {
        this.org = org
    }

    override fun read(): Int = org?.read() ?: -1

}

class JsonRestoreJobTest {

    lateinit var job: JsonRestoreJobTestImpl
    lateinit var source: InputStreamWrapper

    @Before
    fun setUp() {
        source = InputStreamWrapper()
        job = JsonRestoreJobTestImpl(source)
    }

    @Test
    fun readNextArray() {
        val json = "[" +
                    "{\"id\": 1, \"name\": \"Account 1\",\"balance\": 120.0}," +
                    "{\"id\": 2, \"name\": \"Account 2\",\"balance\": 1000.0}" +
                "]"
        source.wrap(json.asInputStream())
        val expected = listOf(
            AccountData(1,"Account 1", 120.0f),
            AccountData(2,"Account 2",1000.0f)
        )
        val actual = job.readNextObjectArray(AccountData::class.java)

        assertEquals(expected,actual)
    }

    @Test
    fun readNextArrayInChunk() {
        val json = "[" +
                    "{\"id\": 1,\"amount\": 100.0, \"type\": \"CREDIT\", \"date\": \"2025-05-06\", \"primaryAccountId\": 1}," +
                    "{\"id\": 2,\"amount\": 150.0, \"type\": \"DEBIT\", \"date\": \"2025-05-06\", \"primaryAccountId\": 1}," +
                    "{\"id\": 3,\"amount\": 120.0, \"type\": \"DEBIT\", \"date\": \"2025-05-06\", \"primaryAccountId\": 2}," +
                    "{\"id\": 4,\"amount\": 80.0, \"type\": \"CREDIT\", \"date\": \"2025-05-06\", \"primaryAccountId\": 2}," +
                    "{\"id\": 5,\"amount\": 1100.0, \"type\": \"CREDIT\", \"date\": \"2025-05-06\", \"primaryAccountId\": 1}," +
                    "{\"id\": 6,\"amount\": 10.0, \"type\": \"DEBIT\", \"date\": \"2025-05-06\", \"primaryAccountId\": 1}" +
                "]"
        source.wrap(json.asInputStream())
        val expected = listOf(
            HistoryData(id=1, amount = 100.0f, type= HistoryType.CREDIT, date= LocalDate.of(2025,5,6), primaryAccountId = 1),
            HistoryData(id=2, amount = 150.0f, type= HistoryType.DEBIT, date= LocalDate.of(2025,5,6), primaryAccountId = 1) ,
            HistoryData(id=3, amount = 120.0f, type= HistoryType.DEBIT, date= LocalDate.of(2025,5,6), primaryAccountId = 2) ,
            HistoryData(id=4, amount = 80.0f, type= HistoryType.CREDIT, date= LocalDate.of(2025,5,6), primaryAccountId = 2),
            HistoryData(id=5, amount = 1100.0f, type= HistoryType.CREDIT, date= LocalDate.of(2025,5,6), primaryAccountId = 1),
            HistoryData(id=6, amount = 10.0f, type= HistoryType.DEBIT, date= LocalDate.of(2025,5,6), primaryAccountId = 1)
        )
        val reader = job.readNextObjectArrayInChunk(HistoryData::class.java)
        val actual = mutableListOf<HistoryData>()
        while (reader.hasNext()) {
            actual.addAll(reader.readNextChunk(2))
        }
        assertEquals(expected,actual)
    }

    @Test
    fun restore() {
        val json = "{\"key1\": {\"data\":10},\"key2\": {\"data\":56}}"
        source.wrap(json.asInputStream())
        job.restore()

        val actual = job.key1
        val expected = SimpleValue(10)

        assertEquals(expected, actual)
    }
}