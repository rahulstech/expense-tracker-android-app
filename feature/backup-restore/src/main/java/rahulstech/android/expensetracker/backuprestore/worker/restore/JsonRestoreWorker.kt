package rahulstech.android.expensetracker.backuprestore.worker.restore

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.ParameterException
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.util.FileUtil
import rahulstech.android.expensetracker.backuprestore.util.NotificationBuilder
import rahulstech.android.expensetracker.backuprestore.util.NotificationConstants
import rahulstech.android.expensetracker.backuprestore.util.Progress
import rahulstech.android.expensetracker.backuprestore.util.createRestoreNotification
import rahulstech.android.expensetracker.backuprestore.worker.job.JsonRestoreJob
import rahulstech.android.expensetracker.domain.ExpenseRepository
import java.io.InputStream

class JsonRestoreWorker(context: Context, parameters: WorkerParameters): Worker(context, parameters) {

    companion object {
        private val TAG = JsonRestoreWorker::class.simpleName
    }

    private val repo = ExpenseRepository.getInstance(context)

    private var job: JsonRestoreJob? = null

    override fun doWork(): Result {
        setForegroundAsync(createForegroundInfo(createRestoreNotification()))
        try {
            val job = JsonRestoreJob.create(
                sourceFactory = this::openInputBackupFile,
                repo = repo.restoreRepository
            )
            this.job = job
            job.progressCallback = { _,progress -> createRestoreNotification(progress) }
            job.restore()
            return Result.success()
        }
        catch (ex: Exception) {
            Log.e(TAG,"restore failed with exception",ex)
            return Result.failure()
        }
    }

    override fun onStopped() {
        super.onStopped()
        job?.terminate()
    }

    private fun openInputBackupFile(): InputStream {
        val backupFile = inputData.getString(Constants.DATA_BACKUP_FILE)
        if (backupFile.isNullOrBlank()) {
            throw ParameterException("DATA_BACKUP_FILE is not found inputData")
        }
        val uri = backupFile.toUri()
        Log.i(TAG, "restore backup file uri $uri")
        return FileUtil.openInputStream(applicationContext, uri)
    }

    private fun createRestoreNotification(workProgress: Progress = Progress.infinite()): Notification {
        val builder = NotificationBuilder(applicationContext).apply {
            message = getStyledMessage()
            progress = workProgress
        }
        setProgressAsync(workProgress.toWorkData())
        return createRestoreNotification(applicationContext, builder)
    }

    private fun createForegroundInfo(notification: Notification): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(NotificationConstants.RESTORE_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
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
}