package rahulstech.android.expensetracker.backuprestore.worker.restore

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.AgentSettingsData
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.util.MoneyTransferData
import rahulstech.android.expensetracker.backuprestore.util.TransactionData
import rahulstech.android.expensetracker.backuprestore.util.newGson
import rahulstech.android.expensetracker.backuprestore.worker.Constants
import rahulstech.android.expensetracker.backuprestore.worker.WriteHelper
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader

class JsonRestoreWorker(context: Context, parameters: WorkerParameters): Worker(context, parameters) {


    companion object {
        private val TAG = JsonRestoreWorker::class.simpleName
    }

    override fun doWork(): Result {

        val gson = newGson()
        val uri = getRestoreFileUri()
        val writeHelper: WriteHelper = WriterHelperImpl(applicationContext)
        var input: InputStream? = null
        var jsonReader: JsonReader? = null

        try {
            input = openRestoreFileForReading(uri)
            jsonReader = gson.newJsonReader(InputStreamReader(input))
            writeHelper.open()
            restore(writeHelper,jsonReader,gson)
        }
        catch (ex: Exception) {
            Log.e(TAG, "JsonRestoreWork failed with exception",ex)
            return Result.failure()
        }
        finally {
            runCatching { writeHelper.close() }.onFailure { Log.e(TAG, "failed to close writerHelper", it) }
            runCatching { jsonReader?.close() }
            runCatching { input?.close() }
        }

        return Result.success()
    }

    fun getRestoreFileUri(): Uri {
        return Uri.parse("")
    }

    fun openRestoreFileForReading(uri: Uri): InputStream {
        return ByteArrayInputStream(byteArrayOf())
    }

    @VisibleForTesting
    fun restore(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            val name = jsonReader.nextName()
            when(name) {
                Constants.JSON_FIELD_ACCOUNTS -> restoreAccounts(writeHelper, jsonReader, gson)
                Constants.JSON_FIELD_GROUPS, Constants.JSON_FIELD_PEOPLE -> restoreGroups(writeHelper, jsonReader, gson)
                Constants.JSON_FIELD_TRANSACTIONS -> restoreTransactions(writeHelper, jsonReader, gson)
                Constants.JSON_FIELD_MONEY_TRANSFER -> restoreMoneyTransfers(writeHelper, jsonReader, gson)
                Constants.JSON_FIELD_HISTORIES -> restoreHistories(writeHelper, jsonReader, gson)
                Constants.JSON_FIELD_APP_SETTINGS -> restoreAppSettings(writeHelper, jsonReader, gson)
                Constants.JSON_FIELD_AGENT_SETTINGS -> restoreAgentSettings(writeHelper, jsonReader, gson)
                else -> jsonReader.skipValue()
            }
        }
        jsonReader.endObject()
    }

    @VisibleForTesting
    fun restoreAccounts(writerHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        jsonReader.beginArray()
        val accounts = mutableListOf<AccountData>()
        while(jsonReader.hasNext()) {
            val account = gson.fromJson(jsonReader, TypeToken.get(AccountData::class.java))
            accounts.add(account)
        }
        jsonReader.endArray()

        writerHelper.writeAccounts(accounts)
        accounts.clear()
    }

    @VisibleForTesting
    fun restoreGroups(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        jsonReader.beginArray()
        val groups = mutableListOf<GroupData>()
        while (jsonReader.hasNext()) {
            val group = gson.fromJson(jsonReader, TypeToken.get(GroupData::class.java))
            groups.add(group)
        }
        jsonReader.endArray()

        writeHelper.writeGroups(groups)
        groups.clear()
    }

    @VisibleForTesting
    fun restoreTransactions(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        jsonReader.beginArray()
        val histories = mutableListOf<HistoryData>()
        while (jsonReader.hasNext()) {
            val transaction = gson.fromJson(jsonReader, TypeToken.get(TransactionData::class.java))
            val history = transaction.toHistoryData()
            histories.add(history)
        }
        jsonReader.endArray()

        writeHelper.writeHistories(histories)
        histories.clear()
    }

    @VisibleForTesting
    fun restoreMoneyTransfers(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        jsonReader.beginArray()
        val histories = mutableListOf<HistoryData>()
        while (jsonReader.hasNext()) {
            val moneyTransfer = gson.fromJson(jsonReader, TypeToken.get(MoneyTransferData::class.java))
            val history = moneyTransfer.toHistoryData()
            histories.add(history)
        }
        jsonReader.endArray()

        writeHelper.writeHistories(histories)
        histories.clear()
    }

    @VisibleForTesting
    fun restoreHistories(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        jsonReader.beginArray()
        val histories = mutableListOf<HistoryData>()
        while (jsonReader.hasNext()) {
            val history = gson.fromJson(jsonReader, TypeToken.get(HistoryData::class.java))
            histories.add(history)
        }
        jsonReader.endArray()

        writeHelper.writeHistories(histories)
        histories.clear()
    }

    @VisibleForTesting
    fun restoreAppSettings(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        val settings = gson.fromJson(jsonReader, TypeToken.get(AppSettingsData::class.java))
        writeHelper.writeAppSettings(settings)
    }

    @VisibleForTesting
    fun restoreAgentSettings(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        val settings = gson.fromJson(jsonReader, TypeToken.get(AgentSettingsData::class.java))
        writeHelper.writeAgentSettings(settings)
    }

    private fun updateProgress(current: Int, message: CharSequence) {

    }
}