package rahulstech.android.expensetracker.backuprestore.worker.restore

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.gson.stream.JsonReader
import dreammaker.android.expensetracker.database.HistoryType
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.util.newGson
import rahulstech.android.expensetracker.backuprestore.Constants
import java.io.InputStreamReader

@RunWith(AndroidJUnit4::class)
class JsonRestoreWork8Test {

    var _worker: JsonRestoreWorker? = null
    val worker: JsonRestoreWorker get() = _worker!!

    val gson = newGson()

    var _jsonReader: JsonReader? = null
    val jsonReader: JsonReader get() = _jsonReader!!

    val writeHelper = FakeWriteHelper()

    fun newJsonReader(json: String) {
        val inputStream = json.trimIndent().byteInputStream(Charsets.UTF_8)
        _jsonReader = gson.newJsonReader(InputStreamReader(inputStream))
    }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        _worker = TestListenableWorkerBuilder<JsonRestoreWorker>(context)
            .setInputData(workDataOf())
            .build()
    }

    @After
    fun cleanup() {
        writeHelper.callback = null
        _jsonReader?.close()
        _worker = null
    }

    @Test
    fun testRestoreAccounts() {
        val json = "{\"accounts\":[{\"id\": 1, \"name\": \"Account 1\", \"balance\": 150.00}]}"
        newJsonReader(json)

        val expected = listOf(AccountData(1,"Account 1", 150.0f))
        writeHelper.callback = { _,actual ->
            assertEquals(expected,actual)
        }

        jsonReader.beginObject()
        jsonReader.nextName()
        worker.restoreAccounts(writeHelper,jsonReader,gson)
    }

    @Test
    fun testRestoreGroups() {
        val json = "{\"groups\":[{\"id\": 1, \"name\": \"Group 1\", \"balance\": 150.00}]}"
        newJsonReader(json)

        val expected = listOf(GroupData(1,"Group 1", 150.0f))
        writeHelper.callback = { _,actual ->
            assertEquals(expected,actual)
        }

        jsonReader.beginObject()
        jsonReader.nextName()
        worker.restoreGroups(writeHelper,jsonReader,gson)
    }

    @Test
    fun  testRestoreHistories() {
        val json = "{\"histories\": [" +
                "{\"id\": 1, \"type\": \"CREDIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": null}," +
                "{\"id\": 2, \"type\": \"DEBIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": null}," +
                "{\"id\": 3, \"type\": \"TRANSFER\", \"primaryAccountId\": 1, \"secondaryAccountId\": 2, \"groupId\": null, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": \"transfer\"}," +
                "{\"id\": 4, \"type\": \"CREDIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": null, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": \"income\"}]}"
        newJsonReader(json)

        val expected = listOf(
            HistoryData(1, HistoryType.CREDIT, 1,null,2,150.00f,Date.valueOf("2023-05-16"),null) ,
            HistoryData(2,HistoryType.DEBIT, 1,null,2,150.00f,Date.valueOf("2023-05-16"),null),
            HistoryData(3,HistoryType.TRANSFER, 1,2,null,150.00f,Date.valueOf("2023-05-16"),"transfer"),
            HistoryData(4,HistoryType.CREDIT, 1,null,null,150.00f,Date.valueOf("2023-05-16"),"income")
        )
        writeHelper.callback = { _,actual ->
            assertEquals(expected,actual)
        }

        jsonReader.beginObject()
        jsonReader.nextName()
        worker.restoreHistories(writeHelper,jsonReader,gson)
    }

    @Test
    fun testRestore() {
        val json = "{ \"version\": 8," +
                "\"accounts\":[{\"id\": 1, \"name\": \"Account 1\", \"balance\": 150.00}]," +
                "\"groups\":[{\"id\": 1, \"name\": \"Group 1\", \"balance\": 150.00}]," +
                "\"histories\": [" +
                "{\"id\": 1, \"type\": \"CREDIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": null}," +
                "{\"id\": 2, \"type\": \"DEBIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": null}," +
                "{\"id\": 3, \"type\": \"TRANSFER\", \"primaryAccountId\": 1, \"secondaryAccountId\": 2, \"groupId\": null, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": \"transfer\"}," +
                "{\"id\": 4, \"type\": \"CREDIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": null, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": \"income\"}]," +
                "\"app_settings\":{}}"
        newJsonReader(json)

        val expectedAccounts = listOf(AccountData(1,"Account 1", 150.0f))
        val expectedPeople = listOf(GroupData(1,"Group 1", 150.0f))
        val expectedHistories = listOf(
            HistoryData(1, HistoryType.CREDIT, 1,null,2,150.00f,Date.valueOf("2023-05-16"),null) ,
            HistoryData(2,HistoryType.DEBIT, 1,null,2,150.00f,Date.valueOf("2023-05-16"),null),
            HistoryData(3,HistoryType.TRANSFER, 1,2,null,150.00f,Date.valueOf("2023-05-16"),"transfer"),
            HistoryData(4,HistoryType.CREDIT, 1,null,null,150.00f,Date.valueOf("2023-05-16"),"income")
        )
        val expectedAppSettings = AppSettingsData()
        writeHelper.callback = { name,actual ->
            when(name) {
                Constants.JSON_FIELD_ACCOUNTS -> assertEquals(expectedAccounts, actual)
                Constants.JSON_FIELD_PEOPLE -> assertEquals(expectedPeople, actual)
                Constants.JSON_FIELD_HISTORIES -> assertEquals(expectedHistories, actual)
                Constants.JSON_FIELD_APP_SETTINGS -> assertEquals(expectedAppSettings, actual)
            }
        }

        worker.restore(writeHelper,jsonReader,gson)
    }
}