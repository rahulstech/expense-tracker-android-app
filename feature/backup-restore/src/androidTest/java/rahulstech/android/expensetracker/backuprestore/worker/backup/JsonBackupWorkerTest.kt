package rahulstech.android.expensetracker.backuprestore.worker.backup

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rahulstech.android.expensetracker.backuprestore.util.newGson
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter

@RunWith(AndroidJUnit4::class)
class JsonBackupWorkerTest {

    var gson: Gson = newGson()

    var _writer: JsonWriter? = null
    val writer: JsonWriter get() = _writer!!

    var _outputStream: OutputStream? = null
    val outputStream: OutputStream get() = _outputStream!!

    var _worker: JsonBackupWorker? = null
    val worker: JsonBackupWorker get() = _worker!!

    val readHelper: JsonBackupWorker.ReadHelper = FakeReadHelper()

    fun getJson(): String {
        val baos = outputStream as ByteArrayOutputStream
        val json = String(baos.toByteArray(), Charsets.UTF_8)
        return json
    }

    @Before
    fun setup() {
        _outputStream = ByteArrayOutputStream()
        _writer = gson.newJsonWriter(OutputStreamWriter(outputStream))

        val context = ApplicationProvider.getApplicationContext<Application>()
        _worker = TestListenableWorkerBuilder<JsonBackupWorker>(context)
            .build()
    }

    @After
    fun cleanup() {
        runCatching { writer.close() }
        runCatching { outputStream.close() }
        _writer = null
        _outputStream = null
        _worker = null
    }

    @Test
    fun testBackupAccount() {
        writer.beginObject()
        worker.backupAccounts(readHelper, writer, gson)
        writer.endObject()
        writer.flush()
        val actual = getJson()
        val expected = "{\"accounts\":[{\"balance\":150.0,\"id\":1,\"name\":\"Account 1\"},{\"balance\":-150.0,\"id\":2,\"name\":\"Account 2\"}]}"
        assertEquals(expected,actual)
    }

    @Test
    fun testBackupGroups() {
        writer.beginObject()
        worker.backupGroups(readHelper, writer, gson)
        writer.endObject()
        writer.flush()
        val actual = getJson()
        val expected = "{\"groups\":[{\"balance\":150.0,\"id\":1,\"name\":\"Group 1\"},{\"balance\":-150.0,\"id\":2,\"name\":\"Group 2\"}]}"
        assertEquals(expected,actual)
    }

    @Test
    fun testBackupHistories() {
        writer.beginObject()
        worker.backupHistories(readHelper, writer, gson, 2)
        writer.endObject()
        writer.flush()
        val actual = getJson()
        val expected = "{\"histories\":[" +
                "{\"amount\":150.0,\"date\":\"2023-05-16\",\"id\":1,\"primaryAccountId\":1,\"type\":\"CREDIT\"}," +
                "{\"amount\":150.0,\"date\":\"2023-05-16\",\"id\":2,\"note\":\"debit\",\"primaryAccountId\":1,\"type\":\"DEBIT\"}," +
                "{\"amount\":150.0,\"date\":\"2023-05-16\",\"groupId\":1,\"id\":3,\"note\":\"credit\",\"primaryAccountId\":1,\"type\":\"CREDIT\"}," +
                "{\"amount\":150.0,\"date\":\"2023-05-16\",\"groupId\":1,\"id\":4,\"primaryAccountId\":1,\"type\":\"DEBIT\"}," +
                "{\"amount\":150.0,\"date\":\"2023-05-16\",\"id\":5,\"primaryAccountId\":1,\"secondaryAccountId\":2,\"type\":\"TRANSFER\"}" +
                "]}".trimIndent()
        assertEquals(expected,actual)
    }

    @Test
    fun testBackupAppSettings() {
        writer.beginObject()
        worker.backupAppSettings(readHelper.readAppSettings(), writer, gson)
        writer.endObject()
        writer.flush()
        val actual = getJson()
        val expected = "{\"app_settings\":{\"viewHistory\":\"DAILY\"}}"
        assertEquals(expected,actual)
    }

    @Test
    fun testBackup() {
        worker.backup(readHelper, writer, gson)
        writer.flush()
        val actual = getJson()
        val expected = "{\"version\":8," +
                "\"accounts\":[{\"balance\":150.0,\"id\":1,\"name\":\"Account 1\"},{\"balance\":-150.0,\"id\":2,\"name\":\"Account 2\"}]," +
                "\"groups\":[{\"balance\":150.0,\"id\":1,\"name\":\"Group 1\"},{\"balance\":-150.0,\"id\":2,\"name\":\"Group 2\"}]," +
                "\"histories\":[{\"amount\":150.0,\"date\":\"2023-05-16\",\"id\":1,\"primaryAccountId\":1,\"type\":\"CREDIT\"}," +
                "{\"amount\":150.0,\"date\":\"2023-05-16\",\"id\":2,\"note\":\"debit\",\"primaryAccountId\":1,\"type\":\"DEBIT\"}," +
                "{\"amount\":150.0,\"date\":\"2023-05-16\",\"groupId\":1,\"id\":3,\"note\":\"credit\",\"primaryAccountId\":1,\"type\":\"CREDIT\"}," +
                "{\"amount\":150.0,\"date\":\"2023-05-16\",\"groupId\":1,\"id\":4,\"primaryAccountId\":1,\"type\":\"DEBIT\"}," +
                "{\"amount\":150.0,\"date\":\"2023-05-16\",\"id\":5,\"primaryAccountId\":1,\"secondaryAccountId\":2,\"type\":\"TRANSFER\"}]," +
                "\"app_settings\":{\"viewHistory\":\"DAILY\"}}".trimIndent()
        assertEquals(expected,actual)
    }

}