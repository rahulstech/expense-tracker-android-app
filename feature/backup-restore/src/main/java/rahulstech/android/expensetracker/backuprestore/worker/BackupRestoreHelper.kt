package rahulstech.android.expensetracker.backuprestore.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rahulstech.android.expensetracker.backuprestore.settings.BackupFrequency
import rahulstech.android.expensetracker.backuprestore.worker.backup.BackupWorker
import rahulstech.android.expensetracker.backuprestore.worker.backup.GZipBackupWorker
import rahulstech.android.expensetracker.backuprestore.worker.backup.JsonBackupWorker
import rahulstech.android.expensetracker.backuprestore.worker.restore.GZipRestoreWorker
import rahulstech.android.expensetracker.backuprestore.worker.restore.JsonRestoreWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

object BackupRestoreHelper {

    private val TAG = BackupRestoreHelper::class.simpleName

    data class ProgressData(
        val workId: UUID,
        val max: Int,
        val current: Int,
        val message: String
    )

    private fun getWorkManager(context: Context): WorkManager {
        return WorkManager.getInstance(context.applicationContext)
    }

    fun startBackup(context: Context, frequency: BackupFrequency) {
        val workManager = getWorkManager(context)
        when(frequency) {
            BackupFrequency.NEVER -> backupOnce(workManager)
            BackupFrequency.DAILY -> backupPeriodic(workManager, 1)
            BackupFrequency.WEEKLY -> backupPeriodic(workManager, 7)
        }
    }

    fun rescheduleBackup(context: Context, frequency: BackupFrequency) {
        val workManager = getWorkManager(context)
        when(frequency) {
            BackupFrequency.NEVER -> workManager.cancelUniqueWork(Constants.TAG_BACKUP_WORK)
            BackupFrequency.DAILY -> backupPeriodic(workManager, 1, TimeUnit.DAYS.toMillis(1))
            BackupFrequency.WEEKLY -> backupPeriodic(workManager, 7, TimeUnit.DAYS.toMillis(7))
        }
    }

    fun startBackupOnce(context: Context) {
        val workManager = getWorkManager(context)
        backupOnce(workManager)
    }

    fun getBackupProgress(context: Context): Flow<ProgressData?> = getProgress(context, Constants.TAG_BACKUP_WORK)

    fun getRestoreProgress(context: Context): Flow<ProgressData?> = getProgress(context, Constants.TAG_RESTORE_WORK)

    private fun getProgress(context: Context, uniqueWorkName: String): Flow<ProgressData?> {
        val workManager = getWorkManager(context)
        return workManager.getWorkInfosForUniqueWorkFlow(uniqueWorkName)
            .map { infos ->
                val runningWorkInfo = infos.find { info -> info.state == WorkInfo.State.RUNNING }
                runningWorkInfo?.let { info ->
                    val progress = info.progress
                    if (progress.keyValueMap.isNotEmpty()) {
                        return@let ProgressData(
                            info.id,
                            progress.getInt(Constants.DATA_PROGRESS_MAX, -1),
                            progress.getInt(Constants.DATA_PROGRESS_CURRENT, -1),
                            progress.getString(Constants.DATA_PROGRESS_MESSAGE) ?: ""
                        )
                    }
                    null
                }
            }
    }

    fun cancelBackup(context: Context) {
        val workManager = getWorkManager(context)
        workManager.cancelUniqueWork(Constants.TAG_BACKUP_WORK)
    }

    private fun backupOnce(workManager: WorkManager) {
        val jsonWorkRequest = OneTimeWorkRequestBuilder<JsonBackupWorker>()
            .addTag(Constants.TAG_JSON_BACKUP_WORK)
            .build()
        val gzipWorkRequest = OneTimeWorkRequestBuilder<GZipBackupWorker>()
            .addTag(Constants.TAG_GZIP_BACKUP_WORK)
            .build()
        workManager
            .beginUniqueWork(Constants.TAG_BACKUP_WORK, ExistingWorkPolicy.REPLACE, jsonWorkRequest)
            .then(gzipWorkRequest)
            .enqueue()
    }

    private fun backupPeriodic(workManager: WorkManager, days: Long, delayMillis: Long = 0) {
        val request = PeriodicWorkRequestBuilder<BackupWorker>(days, TimeUnit.DAYS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(Constants.TAG_BACKUP_WORK, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request)
    }

    fun startRestore(context: Context, backupFile: Uri, mimeType: String, name: String) {
        val workManager = getWorkManager(context)
        when(mimeType) {
            "application/json" -> startRestoreFromJson(workManager, backupFile, name)
            "application/gzip" -> startRestoreFromGZip(workManager, backupFile, name)
            else -> Log.w(TAG,"unknown mime type $mimeType")
        }
    }

    private fun startRestoreFromGZip(workManager: WorkManager, backupFile: Uri, name: String) {
        val inputData = workDataOf(
            Constants.DATA_BACKUP_FILE to backupFile.toString(),
            Constants.DATA_BACKUP_FILE_NAME to name
        )
        val gzipWorkRequest = OneTimeWorkRequestBuilder<GZipRestoreWorker>()
            .addTag(Constants.TAG_GZIP_RESTORE_WORK)
            .setInputData(inputData)
            .build()
        val jsonWorkRequest = OneTimeWorkRequestBuilder<JsonRestoreWorker>()
            .setInputData(inputData)
            .build()
        workManager
            .beginUniqueWork(Constants.TAG_RESTORE_WORK, ExistingWorkPolicy.REPLACE, gzipWorkRequest)
            .then(jsonWorkRequest)
            .enqueue()
    }

    private fun startRestoreFromJson(workManager: WorkManager, backupFile: Uri, name: String) {
        val inputData = workDataOf(
            Constants.DATA_BACKUP_FILE to backupFile.toString(),
            Constants.DATA_BACKUP_FILE_NAME to name
        )
        val request = OneTimeWorkRequestBuilder<JsonRestoreWorker>()
            .addTag(Constants.TAG_JSON_RESTORE_WORK)
            .setInputData(inputData)
            .build()
        workManager.enqueueUniqueWork(Constants.TAG_RESTORE_WORK, ExistingWorkPolicy.REPLACE, request)
    }
}