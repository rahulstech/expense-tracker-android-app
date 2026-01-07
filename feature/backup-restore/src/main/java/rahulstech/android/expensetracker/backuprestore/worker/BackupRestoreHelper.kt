package rahulstech.android.expensetracker.backuprestore.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.settings.BackupFrequency
import rahulstech.android.expensetracker.backuprestore.util.FileEntry
import rahulstech.android.expensetracker.backuprestore.worker.backup.BackupWorker
import rahulstech.android.expensetracker.backuprestore.worker.backup.GZipBackupWorker
import rahulstech.android.expensetracker.backuprestore.worker.backup.JsonBackupWorker
import rahulstech.android.expensetracker.backuprestore.worker.restore.GZipRestoreWorker
import rahulstech.android.expensetracker.backuprestore.worker.restore.JsonRestoreWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ProgressData(
    val workId: UUID,
    val max: Int,
    val current: Int,
    val message: String
)

object BackupRestoreHelper {

    private const val TAG = "BackupRestoreHelper"

    private fun getWorkManager(context: Context): WorkManager {
        return WorkManager.getInstance(context.applicationContext)
    }

    // backup related methods

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
            BackupFrequency.NEVER -> workManager.cancelUniqueWork(Constants.TAG_PERIODIC_BACKUP_WORK)
            BackupFrequency.DAILY -> backupPeriodic(workManager, 1, TimeUnit.DAYS.toMillis(1))
            BackupFrequency.WEEKLY -> backupPeriodic(workManager, 7, TimeUnit.DAYS.toMillis(7))
        }
    }

    fun startBackupOnce(context: Context) {
        backupOnce(getWorkManager(context))
    }

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
        getWorkManager(context).cancelUniqueWork(Constants.TAG_BACKUP_WORK)
    }

    @WorkerThread
    private fun backupOnce(workManager: WorkManager) {
        val jsonWorkRequest = OneTimeWorkRequestBuilder<JsonBackupWorker>()
            .addTag(Constants.TAG_JSON_BACKUP_WORK)
            .build()
        val gzipWorkRequest = OneTimeWorkRequestBuilder<GZipBackupWorker>()
            .addTag(Constants.TAG_GZIP_BACKUP_WORK)
            .build()
        workManager
            .beginUniqueWork(Constants.TAG_BACKUP_WORK, ExistingWorkPolicy.KEEP, jsonWorkRequest)
            .then(gzipWorkRequest)
            .enqueue()
    }

    private fun backupPeriodic(workManager: WorkManager, days: Long, delayMillis: Long = 0) {
        val request = PeriodicWorkRequestBuilder<BackupWorker>(days, TimeUnit.DAYS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            Constants.TAG_PERIODIC_BACKUP_WORK,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )

        // must manually trigger the first work otherwise the periodic work will never run
        backupOnce(workManager)
    }

    // restore related methods

    fun getRestoreProgress(context: Context): Flow<ProgressData?> = getProgress(context, Constants.TAG_RESTORE_WORK)

    fun startRestore(context: Context, file: FileEntry) {
        val workManager = getWorkManager(context)
        when(file.mimeType) {
            "application/gzip" -> startRestoreFromGZip(workManager, file.uri, file.displayName)
            else -> Log.i(TAG,"unknown mime type ${file.mimeType}")
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
            .beginUniqueWork(Constants.TAG_RESTORE_WORK, ExistingWorkPolicy.KEEP, gzipWorkRequest)
            .then(jsonWorkRequest)
            .enqueue()
    }

//    private fun startRestoreFromJson(workManager: WorkManager, backupFile: Uri, name: String) {
//        val inputData = workDataOf(
//            Constants.DATA_BACKUP_FILE to backupFile.toString(),
//            Constants.DATA_BACKUP_FILE_NAME to name
//        )
//        val request = OneTimeWorkRequestBuilder<JsonRestoreWorker>()
//            .addTag(Constants.TAG_JSON_RESTORE_WORK)
//            .setInputData(inputData)
//            .build()
//        workManager.enqueueUniqueWork(Constants.TAG_RESTORE_WORK, ExistingWorkPolicy.REPLACE, request)
//    }

    // helper method

    fun getBackupProgress(context: Context): Flow<ProgressData?> = getProgress(context, Constants.TAG_BACKUP_WORK)
}