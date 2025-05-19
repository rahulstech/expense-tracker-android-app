package rahulstech.android.expensetracker.backuprestore.worker.backup

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.AgentSettingsData
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.util.NotificationBuilder
import rahulstech.android.expensetracker.backuprestore.util.NotificationConstants
import rahulstech.android.expensetracker.backuprestore.util.createBackupNotification
import rahulstech.android.expensetracker.backuprestore.util.newGson
import rahulstech.android.expensetracker.backuprestore.worker.Constants
import rahulstech.android.expensetracker.backuprestore.worker.ReadHelper
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

        private const val MAX_PROGRESS = 5
    }

    override fun doWork(): Result {
        setForegroundAsync(createForegroundInfo(createStartNotification()))
        val gson = newGson()
        val backupFile: File = getBackupFile()
        val readHelper = ReadHelperImpl(applicationContext)
        var output: OutputStream? = null
        var writer: JsonWriter? = null
        try {
            output = openFileOutput(backupFile)
            writer = gson.newJsonWriter(OutputStreamWriter(output))
            readHelper.open()
            backup(readHelper, writer, gson)
        }
        catch (ex: Exception) {
            Log.e(TAG, "JsonBackupWork failed with exception",ex)
            return Result.failure()
        }
        finally {
            setForegroundAsync(createForegroundInfo(createEndNotification()))
            runCatching { readHelper.close() }.onFailure { Log.e(TAG,"fail to close readHelper", it) }
            runCatching { writer?.close() }
            runCatching { output?.close() }
        }

        val data = workDataOf(
            Constants.DATA_JSON_BACKUP_FILE to backupFile.canonicalPath
        )
        return Result.success(data)
    }

    override fun setProgressAsync(data: Data): ListenableFuture<Void> {
        val future = super.setProgressAsync(data)
        val message = data.getString(Constants.DATA_PROGRESS_MESSAGE) ?: ""
        val current = data.getInt(Constants.DATA_PROGRESS_CURRENT, -1)
        val max = data.getInt(Constants.DATA_PROGRESS_MAX, -1)
        setForegroundAsync(createForegroundInfo(createProgressNotification(message, max, current)))
        return future
    }

    private fun getBackupFile(): File {
        val dirFiles = applicationContext.getExternalFilesDir(null)
        val dirBackup = File(dirFiles, Constants.DIR_BACKUP)
        val backupFile = File(dirBackup, BACKUP_FILENAME)
        return backupFile
    }

    private fun openFileOutput(file: File): OutputStream {
        Log.i(TAG, "openFileOutput: file=${file.canonicalPath}")
        file.parentFile?.let { parent ->
            if (!parent.exists()) {
                parent.mkdirs()
            }
        }
        file.deleteOnExit()
        return FileOutputStream(file)
    }

    @VisibleForTesting
    fun backup(readHelper: ReadHelper, writer: JsonWriter, gson: Gson) {
        writer.beginObject()

        // write schema version
        writer.name(Constants.JSON_FIELD_VERSION)
        writer.value(SCHEMA_VERSION)
        writer.flush()

        // backup the database
        // backup accounts
        backupAccounts(readHelper, writer, gson)
        // backup groups
        backupGroups(readHelper, writer, gson)
        // backup histories
        backupHistories(readHelper, writer, gson)

        // backup settings
        val appSettings = readHelper.readAppSettings()
        val agentSettings = readHelper.readAgentSettings()
        backupAppSettings(appSettings, writer, gson)
        backupAgentSettings(agentSettings,writer,gson)

        writer.endObject()
        writer.flush()
    }

    @VisibleForTesting
    fun backupAccounts(readHelper: ReadHelper, writer: JsonWriter, gson: Gson) {
        updateProgress(1, R.string.message_json_backup_accounts)
        val accounts = readHelper.readAccounts(0, BUFFER_SIZE)
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
        updateProgress(2, R.string.message_json_backup_groups)
        val groups = readHelper.readGroups(0, BUFFER_SIZE)
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
        updateProgress(3, R.string.message_json_backup_histories)
        writer.name(Constants.JSON_FIELD_HISTORIES)
        writer.beginArray()
        var offset: Long = 0
        while (true) {
            val histories = readHelper.readHistories(offset, size)
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
        updateProgress(4, R.string.message_json_backup_app_settings)
        writer.name(Constants.JSON_FIELD_APP_SETTINGS)
        gson.toJson(data, AppSettingsData::class.java, writer)
        writer.flush()
    }

    @VisibleForTesting
    fun backupAgentSettings(data: AgentSettingsData, writer: JsonWriter, gson: Gson) {
        updateProgress(5, R.string.message_json_backup_agent_setting)
        writer.name(Constants.JSON_FIELD_AGENT_SETTINGS)
        gson.toJson(data, AgentSettingsData::class.java, writer)
        writer.flush()
    }

    private fun updateProgress(current: Int, @StringRes messageId: Int) {
        updateProgress(current, applicationContext.getString(messageId))
    }

    private fun updateProgress(current: Int, message: CharSequence) {
        setProgressAsync(workDataOf(
            Constants.DATA_PROGRESS_MAX to MAX_PROGRESS,
            Constants.DATA_PROGRESS_CURRENT to current,
            Constants.DATA_PROGRESS_MESSAGE to message
        ))
    }

    private fun createForegroundInfo(notification: Notification): ForegroundInfo {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NotificationConstants.BACKUP_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NotificationConstants.BACKUP_NOTIFICATION_ID, notification)
        }
        return info
    }

    private fun createStartNotification(): Notification {
        val builder = NotificationBuilder().apply {
            setTitleResource(R.string.notification_title_backup)
            setMessageResource(R.string.message_json_backup_start)
        }
        return createBackupNotification(applicationContext, builder)
    }

    private fun createProgressNotification(message: CharSequence, max: Int, current: Int): Notification {
        val builder = NotificationBuilder().apply {
            setTitleResource(R.string.notification_title_backup)
            setMessage(message)
            setProgress(current,max)
        }
        return createBackupNotification(applicationContext, builder)
    }

    private fun createEndNotification(): Notification {
        val builder = NotificationBuilder().apply {
            setTitleResource(R.string.notification_title_backup)
            setMessageResource(R.string.message_json_backup_complete)
        }
        return createBackupNotification(applicationContext, builder)
    }
}