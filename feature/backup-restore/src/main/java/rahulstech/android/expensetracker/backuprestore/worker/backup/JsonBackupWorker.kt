package rahulstech.android.expensetracker.backuprestore.worker.backup

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.util.NotificationBuilder
import rahulstech.android.expensetracker.backuprestore.util.NotificationConstants
import rahulstech.android.expensetracker.backuprestore.util.Progress
import rahulstech.android.expensetracker.backuprestore.util.createBackupNotification
import rahulstech.android.expensetracker.backuprestore.worker.job.JsonBackupJob
import rahulstech.android.expensetracker.domain.ExpenseRepository
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class JsonBackupWorker(context: Context, params: WorkerParameters): Worker(context,params) {

    companion object {
        private val TAG = JsonBackupWorker::class.simpleName
        private const val SCHEMA_VERSION = 8
        private const val BACKUP_FILENAME = "backup.json"
    }

    private val repo = ExpenseRepository.getInstance(applicationContext).backupRepository
    private var job: JsonBackupJob? = null

    override fun doWork(): Result {
        val backupFile: File = getBackupFile()
        try {
            val job = JsonBackupJob.create(SCHEMA_VERSION,repo) { openOutputStream(getBackupFile()) }
            job.progressCallback = { _,progress -> updateProgress(progress) }
            this.job = job

            job.use {
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
    }

    override fun onStopped() {
        super.onStopped()
        job?.terminate()
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

    private fun updateProgress(currentProgress: Progress) {
        val messageBody = applicationContext.getString(R.string.message_backup_progress)
        setProgressAsync(workDataOf(
            Constants.DATA_PROGRESS_MAX to currentProgress.max,
            Constants.DATA_PROGRESS_CURRENT to currentProgress.current,
            Constants.DATA_PROGRESS_MESSAGE to messageBody
        ))
        val builder = NotificationBuilder(applicationContext).apply {
            message = messageBody
            progress = currentProgress
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
}