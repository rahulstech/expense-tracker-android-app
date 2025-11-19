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
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.util.newGson
import rahulstech.android.expensetracker.backuprestore.Constants
import java.io.InputStreamReader

@RunWith(AndroidJUnit4::class)
class JsonRestoreWork7Test {

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
        val json = "{\"accounts\":[{\"_id\": 1, \"account_name\": \"Account 1\", \"balance\": 150.00}]}"
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
    fun testRestorePeople() {
        val json = "{\"people\":[{\"_id\": 1, \"person_name\": \"Person 1\", \"due\": 150.00}]}"
        newJsonReader(json)

        val expected = listOf(GroupData(1,"Person 1", 150.0f))
        writeHelper.callback = { _,actual ->
            assertEquals(expected,actual)
        }

        jsonReader.beginObject()
        jsonReader.nextName()
        worker.restoreGroups(writeHelper,jsonReader,gson)
    }

    @Test
    fun  testRestoreTransactions() {
        val json = "{\"transactions\":[" +
                "{\"_id\": 1, \"amount\": 150.00, \"date\": \"2023-05-16\", \"type\": 0, \"deleted\": false, \"account_id\": 1, \"person_id\": 1}," +
                "{\"_id\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"type\": 0, \"deleted\": true, \"account_id\": 1, \"person_id\": 1, \"description\": null}," +
                "{\"_id\": 3, \"amount\": 150.00, \"date\": \"2023-05-16\", \"type\": 1, \"deleted\": false, \"account_id\": 1, \"person_id\": null, \"description\": \"expense\"}]}"
        newJsonReader(json)

        val expected = listOf(
            HistoryData(1, HistoryType.DEBIT, 1, null, 1, 150.0f, Date(2023,4,16), null),
            HistoryData(2, HistoryType.DEBIT, 1, null, 1, 150.0f, Date(2023,4,16), null, true),
            HistoryData(3, HistoryType.CREDIT, 1, null, null, 150.0f, Date(2023,4,16), "expense")
        )
        writeHelper.callback = { _,actual ->
            assertEquals(expected,actual)
        }

        jsonReader.beginObject()
        jsonReader.nextName()
        worker.restoreTransactions(writeHelper,jsonReader,gson)
    }

    @Test
    fun testRestoreMoneyTransfers() {
        val json = "{\"money_transfers\": [" +
                "{\"id\": 1, \"amount\": 150.00, \"when\": \"2023-05-16\", \"payee_account_id\": 2, \"payer_account_id\": 1, \"description\": null}, " +
                "{\"id\": 2, \"amount\": 140.33, \"when\": \"2023-05-16\", \"payee_account_id\": 2, \"payer_account_id\": 1, \"description\": \"transfer\"}" +
                "]}"
        newJsonReader(json)

        val expected = listOf(
            HistoryData(1, HistoryType.TRANSFER, 1, 2, null, 150.0f, Date(2023,4,16), null),
            HistoryData(2, HistoryType.TRANSFER, 1, 2, null, 140.33f, Date(2023,4,16), "transfer")
        )
        writeHelper.callback = { _,actual ->
            assertEquals(expected,actual)
        }

        jsonReader.beginObject()
        jsonReader.nextName()
        worker.restoreMoneyTransfers(writeHelper,jsonReader,gson)
    }

    @Test
    fun testRestore() {
        val json = "{ \"version\": 7," +
                "\"accounts\":[{\"_id\": 1, \"account_name\": \"Account 1\", \"balance\": 150.00}]," +
                "\"people\":[{\"_id\": 1, \"person_name\": \"Person 1\", \"due\": 150.00}]," +
                "\"transactions\":[" +
                "{\"_id\": 1, \"amount\": 150.00, \"date\": \"2023-05-16\", \"type\": 0, \"deleted\": false, \"account_id\": 1, \"person_id\": 1}," +
                "{\"_id\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"type\": 0, \"deleted\": true, \"account_id\": 1, \"person_id\": 1, \"description\": null}," +
                "{\"_id\": 3, \"amount\": 150.00, \"date\": \"2023-05-16\", \"type\": 1, \"deleted\": false, \"account_id\": 1, \"person_id\": null, \"description\": \"expense\"}]," +
                "\"money_transfers\": [" +
                "{\"id\": 1, \"amount\": 150.00, \"when\": \"2023-05-16\", \"payee_account_id\": 2, \"payer_account_id\": 1, \"description\": null}, " +
                "{\"id\": 2, \"amount\": 140.33, \"when\": \"2023-05-16\", \"payee_account_id\": 2, \"payer_account_id\": 1, \"description\": \"transfer\"}" +
                "]," +
                "\"settings\": {}" +
                "}"
        newJsonReader(json)

        val expectedAccounts = listOf(AccountData(1,"Account 1", 150.0f))
        val expectedPeople = listOf(GroupData(1,"Person 1", 150.0f))
        val expectedTransactions = listOf(
            HistoryData(1, HistoryType.DEBIT, 1, null, 1, 150.0f, Date(2023,4,16), null),
            HistoryData(2, HistoryType.DEBIT, 1, null, 1, 150.0f, Date(2023,4,16), null, true),
            HistoryData(3, HistoryType.CREDIT, 1, null, null, 150.0f, Date(2023,4,16), "expense")
        )
        val expectedMoneyTransfers = listOf(
            HistoryData(1, HistoryType.TRANSFER, 1, 2, null, 150.0f, Date(2023,4,16), null),
            HistoryData(2, HistoryType.TRANSFER, 1, 2, null, 140.33f, Date(2023,4,16), "transfer")
        )
        writeHelper.callback = { name,actual ->
            when(name) {
                Constants.JSON_FIELD_ACCOUNTS -> assertEquals(expectedAccounts, actual)
                Constants.JSON_FIELD_PEOPLE -> assertEquals(expectedPeople, actual)
                Constants.JSON_FIELD_TRANSACTIONS -> assertEquals(expectedTransactions, actual)
                Constants.JSON_FIELD_MONEY_TRANSFER -> assertEquals(expectedMoneyTransfers, actual)
            }
        }

        worker.restore(writeHelper,jsonReader,gson)
    }
}