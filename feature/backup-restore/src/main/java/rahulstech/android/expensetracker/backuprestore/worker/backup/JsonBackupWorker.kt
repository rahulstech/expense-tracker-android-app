package rahulstech.android.expensetracker.backuprestore.worker.backup

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.util.NotificationConstants
import rahulstech.android.expensetracker.backuprestore.util.createBackupNotification
import rahulstech.android.expensetracker.backuprestore.worker.job.JsonBackupJob
import rahulstech.android.expensetracker.domain.ExpenseRepository
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class JsonBackupWorker(context: Context, params: WorkerParameters): CoroutineWorker(context,params) {

    companion object {
        private val TAG = JsonBackupWorker::class.simpleName
        private const val SCHEMA_VERSION = 8
        private const val BACKUP_FILENAME = "backup.json"
    }

    private val repo = ExpenseRepository.getInstance(applicationContext).backupRepository
    private var job: JsonBackupJob? = null

    override suspend fun doWork(): Result {
        startForeground()
        val backupFile: File = getBackupFile()
        try {
             job = JsonBackupJob.create(
                version = SCHEMA_VERSION,
                repo = repo,
                destFactory = { openOutputStream(getBackupFile()) }
            )
            job?.use {
                it.backup()
            }

            val resultData = workDataOf(
            Constants.DATA_BACKUP_FILE to backupFile.canonicalPath
            )
            return Result.success(resultData)
        }
        catch(ex: Exception) {
            Log.e(TAG, "error during backup", ex)
            return Result.failure()
        }
        finally {
            job?.terminate()
        }
    }

    private fun getBackupFile(): File {
        val dirBackup = applicationContext.cacheDir
        val backupFile = File(dirBackup, BACKUP_FILENAME)
        return backupFile
    }

    private fun openOutputStream(file: File): OutputStream {
        Log.d(TAG, "openOutputStream: file=${file.canonicalPath}")
        if (file.exists()) {
            file.delete()
            Log.w(TAG, "openOutputStream: existing file=${file.canonicalPath} deleted")
        }
        return FileOutputStream(file)
    }

    private suspend fun startForeground() {
        setForeground(createForegroundInfo())
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val message = applicationContext.getString(R.string.notification_message_backup)
        val notification = createBackupNotification(applicationContext, message)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(NotificationConstants.BACKUP_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NotificationConstants.BACKUP_NOTIFICATION_ID, notification)
        }
    }
}
