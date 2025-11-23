package rahulstech.android.expensetracker.backuprestore.worker.backup

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import dreammaker.android.expensetracker.database.ExpensesDatabase
import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.util.NotificationBuilder
import rahulstech.android.expensetracker.backuprestore.util.NotificationConstants
import rahulstech.android.expensetracker.backuprestore.util.createBackupNotification
import rahulstech.android.expensetracker.backuprestore.util.newGson
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter

class JsonBackupWorker(context: Context, params: WorkerParameters): Worker(context,params) {

    companion object {
        private val TAG = JsonBackupWorker::class.simpleName
        private const val BUFFER_SIZE = 1000L
        private const val SCHEMA_VERSION = 8
        private const val BACKUP_FILENAME = "backup.json"
        private const val MAX_PROGRESS = 4
    }

    override fun doWork(): Result {
        val gson = newGson()
        var output: OutputStream? = null
        var writer: JsonWriter? = null
        val backupFile: File = getBackupFile()
        val readHelper: ReadHelper = ReadHelperImpl(applicationContext)

        updateProgress(0)
        try {
            output = openOutputStream(backupFile)
            writer = gson.newJsonWriter(OutputStreamWriter(output))
            backup(readHelper, writer, gson)
        }
        catch (ex: Exception) {
            Log.e(TAG, "JsonBackupWork failed with exception",ex)
            runCatching { backupFile.delete() }
            return Result.failure()
        }
        finally {
            runCatching { readHelper.close() }.onFailure { Log.e(TAG,"fail to close readHelper", it) }
            runCatching { writer?.close() }
            runCatching { output?.close() }
        }

        val resultData = workDataOf(
            Constants.DATA_BACKUP_FILE to backupFile.canonicalPath
        )
        return Result.success(resultData)
    }

    private fun getBackupFile(): File {
        val dirBackup = applicationContext.cacheDir
        val backupFile = File(dirBackup, BACKUP_FILENAME)
        return backupFile
    }

    private fun openOutputStream(file: File): OutputStream {
        Log.i(TAG, "openOutputStream: file=${file.canonicalPath}")
        if (file.exists()) {
            file.delete()
            Log.i(TAG, "openOutputStream: existing file=${file.canonicalPath} deleted")
        }
        return FileOutputStream(file)
    }

    @VisibleForTesting
    fun backup(readHelper: ReadHelper, writer: JsonWriter, gson: Gson) {

        try {
            readHelper.open()

            throwIfStopped()
            writer.beginObject()

            // write schema version
            throwIfStopped()
            writer.name(Constants.JSON_FIELD_VERSION)
            writer.value(SCHEMA_VERSION)
            writer.flush()

            // backup the database
            // backup accounts
            throwIfStopped()
            updateProgress(1)
            backupAccounts(readHelper, writer, gson)

            // backup groups
            throwIfStopped()
            updateProgress(2)
            backupGroups(readHelper, writer, gson)

            // backup histories
            throwIfStopped()
            updateProgress(3)
            backupHistories(readHelper, writer, gson)

            // backup settings
            throwIfStopped()
            updateProgress(4)
//            val appSettings = readHelper.readAppSettings()
//            backupAppSettings(appSettings, writer, gson)

            writer.endObject()
            writer.flush()
        }
        finally {
            runCatching { readHelper.close() }.onFailure { Log.e(TAG,"fail to close readHelper", it) }
        }
    }

    @VisibleForTesting
    fun backupAccounts(readHelper: ReadHelper, writer: JsonWriter, gson: Gson) {
        val accounts = readHelper.readAccounts()
        writer.name(Constants.JSON_FIELD_ACCOUNTS)
        writer.beginArray()
        accounts.forEach { data ->
            gson.toJson(data, AccountData::class.java, writer)
        }
        writer.endArray()
        writer.flush()
    }

    @VisibleForTesting
    fun backupGroups(readHelper: ReadHelper, writer: JsonWriter, gson: Gson) {
        val groups = readHelper.readGroups()
        writer.name(Constants.JSON_FIELD_GROUPS)
        writer.beginArray()
        groups.forEach { data ->
            gson.toJson(data, GroupData::class.java, writer)
        }
        writer.endArray()
        writer.flush()
    }

    @VisibleForTesting
    fun backupHistories(readHelper: ReadHelper, writer: JsonWriter, gson: Gson, size: Long = BUFFER_SIZE) {
        writer.name(Constants.JSON_FIELD_HISTORIES)
        writer.beginArray()
        var offset: Long = 0
        while (true) {
            val histories = readHelper.readHistories(offset, size) as List<*>
            if (histories.isEmpty()) {
                break
            }
            histories.forEach { data ->
                gson.toJson(data, HistoryData::class.java, writer)
            }
            offset += histories.size.toLong()
        }
        writer.endArray()
        writer.flush()
    }

    @VisibleForTesting
    fun backupAppSettings(data: AppSettingsData, writer: JsonWriter, gson: Gson) {
        writer.name(Constants.JSON_FIELD_APP_SETTINGS)
        gson.toJson(data, AppSettingsData::class.java, writer)
        writer.flush()
    }

    private fun updateProgress(current: Int) {
        val message = applicationContext.getString(R.string.message_backup_progress)
        setProgressAsync(workDataOf(
            Constants.DATA_PROGRESS_MAX to MAX_PROGRESS,
            Constants.DATA_PROGRESS_CURRENT to current,
            Constants.DATA_PROGRESS_MESSAGE to message
        ))
        val builder = NotificationBuilder().apply {
            setMessage(message)
            setProgress(current, MAX_PROGRESS)
        }
        val notification = createBackupNotification(applicationContext, builder)
        setForegroundAsync(createForegroundInfo(notification))
    }

    private fun createForegroundInfo(notification: Notification): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(NotificationConstants.BACKUP_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NotificationConstants.BACKUP_NOTIFICATION_ID, notification)
        }
    }

    private fun throwIfStopped() {
        if (isStopped) {
            throw IllegalStateException("JsonBackupWorker was stopped before finished")
        }
    }


    
    interface ReadHelper {

        fun open()

        fun close()

        fun readAccounts(): List<AccountData>

        fun readGroups(): List<GroupData>

        fun readHistories(from: Long, size: Long): List<HistoryData>
    }

    private class ReadHelperImpl(context: Context): ReadHelper {

        private val applicationContext = context.applicationContext
        private var _expenseDB: ExpensesDatabase? = null
        private val expenseDB: ExpensesDatabase get() = _expenseDB!!

        override fun open() {
//            _expenseDB = ExpensesDatabase.getInstance(applicationContext)
        }

        override fun close() {
//            _expenseDB?.close()
        }

        override fun readAccounts(): List<AccountData> {
//            return expenseDB.accountDao.getAccounts().map { account -> account.toAccountData() }
            return emptyList()
        }

        override fun readGroups(): List<GroupData> {
//            return expenseDB.groupDao.getGroups().map { group -> group.toGroupData() }
            return emptyList()
        }

        override fun readHistories(from: Long, size: Long): List<HistoryData> {
//            return expenseDB.historyDao.getAllHistories(from, size).map { history -> history.toHistoryData() }
            return emptyList()
        }
    }
}