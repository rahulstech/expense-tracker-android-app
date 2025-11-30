package rahulstech.android.expensetracker.backuprestore.worker.job

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import rahulstech.android.expensetracker.backuprestore.FakeRestoreRepositoryImpl
import rahulstech.android.expensetracker.backuprestore.SimpleData
import rahulstech.android.expensetracker.backuprestore.VersionException
import rahulstech.android.expensetracker.backuprestore.asInputStream
import rahulstech.android.expensetracker.backuprestore.worker.job.impl.restore.JsonRestoreJobV8Impl
import java.io.InputStream


class JsonRestoreJobTestImpl(source: InputStream): JsonRestoreJob(1,source) {

    var key1: SimpleData? = null

    override fun doRestore() {
        while (hasNext()) {
            val name = readNextName()
            when(name) {
                "key1" -> {
                    key1 = readNextObject(SimpleData::class.java)
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
                    "{\"data\":1}," +
                    "{\"data\":2}" +
                "]"
        source.wrap(json.asInputStream())
        val expected = listOf(
            SimpleData(1),
            SimpleData(2)
        )
        val actual = job.readNextObjectArray(SimpleData::class.java)

        assertEquals(expected,actual)
    }

    @Test
    fun readNextArrayInChunk() {
        val json = "[" +
                    "{\"data\":1}," +
                    "{\"data\":2}," +
                    "{\"data\":3}," +
                    "{\"data\":4}," +
                    "{\"data\":5}" +
                "]"
        source.wrap(json.asInputStream())
        val expected = listOf(
            SimpleData(1),
            SimpleData(2),
            SimpleData(3),
            SimpleData(4),
            SimpleData(5)
        )
        val reader = job.readNextObjectArrayInChunk(SimpleData::class.java)
        val actual = mutableListOf<SimpleData>()
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
        val expected = SimpleData(10)

        assertEquals(expected, actual)
    }

    @Test
    fun readVersion() {
        val json = "{\"version\": 8}"
        val version = JsonRestoreJob.readVersion(json.byteInputStream(Charsets.UTF_8))
        assertEquals(8, version)
    }

    @Test
    fun create_throwsVersionException() {
        val json = "{\"version\":10}"
        assertThrows(VersionException::class.java) {
            JsonRestoreJob.create(FakeRestoreRepositoryImpl()) { json.asInputStream() }
        }
    }

    @Test
    fun create() {
        val json = "{\"version\":8}"
        val job = JsonRestoreJob.create(FakeRestoreRepositoryImpl()) { json.asInputStream() }
        assertTrue(job is JsonRestoreJobV8Impl)
    }
}