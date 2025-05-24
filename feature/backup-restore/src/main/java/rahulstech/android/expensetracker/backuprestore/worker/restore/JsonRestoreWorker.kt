package rahulstech.android.expensetracker.backuprestore.worker.restore

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.settings.SettingsProvider
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.util.FileUtil
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.util.MoneyTransferData
import rahulstech.android.expensetracker.backuprestore.util.NotificationBuilder
import rahulstech.android.expensetracker.backuprestore.util.NotificationConstants
import rahulstech.android.expensetracker.backuprestore.util.TransactionData
import rahulstech.android.expensetracker.backuprestore.util.createRestoreNotification
import rahulstech.android.expensetracker.backuprestore.util.newGson
import rahulstech.android.expensetracker.backuprestore.worker.Constants
import java.io.InputStream
import java.io.InputStreamReader

class JsonRestoreWorker(context: Context, parameters: WorkerParameters): Worker(context, parameters) {
    private val TAG = JsonRestoreWorker::class.simpleName

    override fun doWork(): Result {
        setForegroundAsync(createForegroundInfo(createRestoreNotification()))
        val gson = newGson()
        val writeHelper: WriteHelper = WriterHelperImpl(applicationContext)
        try {
            val input = openInputBackupFile()
            input.use {
                val jsonReader = gson.newJsonReader(InputStreamReader(input))
                jsonReader.use { restore(writeHelper,jsonReader,gson) }
            }
        }
        catch (ex: Exception) {
            Log.e(TAG, "JsonRestoreWork failed with exception",ex)
            return Result.failure()
        }
        return Result.success()
    }

    private fun openInputBackupFile(): InputStream {
        val uri = Uri.parse(inputData.getString(Constants.DATA_BACKUP_FILE))
        Log.i(TAG, "restore backup file $uri")
        return FileUtil.openInputStream(applicationContext, uri)
    }

    @VisibleForTesting
    fun restore(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        try {
            writeHelper.open()

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
                    else -> jsonReader.skipValue()
                }
            }
            jsonReader.endObject()
        }
        finally {
            runCatching { writeHelper.close() }.onFailure { Log.e(TAG, "failed to close writerHelper", it) }
        }
    }

    @VisibleForTesting
    fun restoreAccounts(writerHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        throwIfStopped()

        jsonReader.beginArray()
        val accounts = mutableListOf<AccountData>()
        while(jsonReader.hasNext()) {
            throwIfStopped()
            val account = gson.fromJson(jsonReader, TypeToken.get(AccountData::class.java))
            accounts.add(account)
        }
        jsonReader.endArray()

        throwIfStopped()
        writerHelper.writeAccounts(accounts)
        accounts.clear()
    }

    @VisibleForTesting
    fun restoreGroups(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        throwIfStopped()

        jsonReader.beginArray()
        val groups = mutableListOf<GroupData>()
        while (jsonReader.hasNext()) {
            throwIfStopped()
            val group = gson.fromJson(jsonReader, TypeToken.get(GroupData::class.java))
            groups.add(group)
        }
        jsonReader.endArray()

        throwIfStopped()
        writeHelper.writeGroups(groups)
        groups.clear()
    }

    @VisibleForTesting
    fun restoreTransactions(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        throwIfStopped()

        jsonReader.beginArray()
        val histories = mutableListOf<HistoryData>()
        while (jsonReader.hasNext()) {
            throwIfStopped()
            val transaction = gson.fromJson(jsonReader, TypeToken.get(TransactionData::class.java))
            val history = transaction.toHistoryData()
            histories.add(history)
        }
        jsonReader.endArray()

        throwIfStopped()
        writeHelper.writeHistories(histories)
        histories.clear()
    }

    @VisibleForTesting
    fun restoreMoneyTransfers(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        throwIfStopped()

        jsonReader.beginArray()
        val histories = mutableListOf<HistoryData>()
        while (jsonReader.hasNext()) {
            throwIfStopped()
            val moneyTransfer = gson.fromJson(jsonReader, TypeToken.get(MoneyTransferData::class.java))
            val history = moneyTransfer.toHistoryData()
            histories.add(history)
        }
        jsonReader.endArray()

        throwIfStopped()
        writeHelper.writeHistories(histories)
        histories.clear()
    }

    @VisibleForTesting
    fun restoreHistories(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        throwIfStopped()

        jsonReader.beginArray()
        val histories = mutableListOf<HistoryData>()
        while (jsonReader.hasNext()) {
            throwIfStopped()
            val history = gson.fromJson(jsonReader, TypeToken.get(HistoryData::class.java))
            histories.add(history)
        }
        jsonReader.endArray()

        throwIfStopped()
        writeHelper.writeHistories(histories)
        histories.clear()
    }

    @VisibleForTesting
    fun restoreAppSettings(writeHelper: WriteHelper, jsonReader: JsonReader, gson: Gson) {
        throwIfStopped()
        val settings = gson.fromJson(jsonReader, TypeToken.get(AppSettingsData::class.java))
        writeHelper.writeAppSettings(settings)
    }

    private fun createRestoreNotification(): Notification {
        val builder = NotificationBuilder().apply {
            setMessage(getStyledMessage())
        }
        val progressData = workDataOf(
            Constants.DATA_PROGRESS_MESSAGE to getPlainMessage()
        )
        setProgressAsync(progressData)
        return createRestoreNotification(applicationContext, builder)
    }

    private fun createForegroundInfo(notification: Notification): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(NotificationConstants.RESTORE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NotificationConstants.RESTORE_NOTIFICATION_ID, notification)
        }
    }

    private fun getStyledMessage(): CharSequence {
        val filename = inputData.getString(Constants.DATA_BACKUP_FILE_NAME)
        val label = applicationContext.getString(R.string.message_json_restore)
        return buildSpannedString {
            append(label)
            append(" ")
            bold { append(filename) }
        }
    }

    private fun getPlainMessage(): CharSequence {
        val filename = inputData.getString(Constants.DATA_BACKUP_FILE_NAME)
        val label = applicationContext.getString(R.string.message_json_restore)
        return buildString {
            append(label)
            append(" ")
           append(filename)
        }
    }

    private fun throwIfStopped() {
        if (isStopped) {
            throw IllegalStateException("JsonBackupWorker was stopped before finished")
        }
    }


    interface WriteHelper {

        fun open()

        fun close()

        fun writeAccounts(accounts: List<AccountData>)

        fun writeGroups(groups: List<GroupData>)

        fun writeHistories(histories: List<HistoryData>)

        fun writeAppSettings(settings: AppSettingsData)
    }

    private class WriterHelperImpl(context: Context): WriteHelper {

        private val applicationContext = context.applicationContext
        private var _expenseDB: ExpensesDatabase? = null
        private val expenseDB: ExpensesDatabase get() = _expenseDB!!

        override fun open() {
            // open expense db connection
            _expenseDB = ExpensesDatabase.getInstance(applicationContext)
        }

        override fun close() {
            // close expense db connection
            _expenseDB?.close()
            _expenseDB = null
        }

        override fun writeAccounts(accounts: List<AccountData>) {
            val dao = expenseDB.accountDao
            val dbAccounts = accounts.map { account -> account.toAccountModel().toAccount() }
            dao.insertAccounts(dbAccounts)
        }

        override fun writeGroups(groups: List<GroupData>) {
            val dao = expenseDB.groupDao
            val dbGroups = groups.map { group -> group.toGroupModel().toGroup() }
            dao.insertGroups(dbGroups)
        }

        override fun writeHistories(histories: List<HistoryData>) {
            val dao = expenseDB.historyDao
            val dbHistories = histories.map { history -> history.toHistoryModel().toHistory() }
            dao.insertHistories(dbHistories)
        }

        override fun writeAppSettings(settings: AppSettingsData) {
            val model = settings.toSettingsModel()
            SettingsProvider.get(applicationContext).restore(model)
        }
    }
}