package rahulstech.android.expensetracker.backuprestore.worker.job

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import rahulstech.android.expensetracker.backuprestore.SimpleData
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class JsonBackupJobTestImpl(destination: OutputStream): JsonBackupJob(1,destination) {
    override fun doBackup() {
        writeArray("key",listOf(
            SimpleData(1),
            SimpleData(2)
        ))
    }
}

class JsonBackupJobTest {

    lateinit var dest: ByteArrayOutputStream
    lateinit var job: JsonBackupJobTestImpl

    @Before
    fun setUp() {
        dest = ByteArrayOutputStream()
        job = JsonBackupJobTestImpl(dest)
    }

    @Test
    fun writeInt() {
        job.beginObject()
        job.writeInt("key",1)
        job.endObject()
        val expected = "{\"key\":1}"
        val actual = dest.toString(Charsets.UTF_8)
        assertEquals(expected,actual)
    }

    @Test
    fun writeArray() {
        val value = listOf(
            SimpleData(1),
            SimpleData(2)
        )
        job.beginObject()
        job.writeArray("key",value)
        job.endObject()
        val expected = "{\"key\":[{\"data\":1},{\"data\":2}]}"
        val actual = dest.toString(Charsets.UTF_8)
        assertEquals(expected,actual)
    }

    @Test
    fun writeArrayInChunk() {
        job.beginObject()
        job.writeArrayInChunk<SimpleData>("key").use { writer ->
            writer.writeNextChunk(listOf(SimpleData(1)))
            writer.writeNextChunk(listOf(SimpleData(2)))
        }
        job.endObject()
        val expected = "{\"key\":[{\"data\":1},{\"data\":2}]}"
        val actual = dest.toString(Charsets.UTF_8)
        assertEquals(expected,actual)
    }

    @Test
    fun backup() {
        job.backup()
        val expected = "{\"version\":1,\"key\":[{\"data\":1},{\"data\":2}]}"
        val actual = dest.toString(Charsets.UTF_8)
        assertEquals(expected,actual)
    }
}