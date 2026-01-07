package rahulstech.android.expensetracker.backuprestore.worker.restore

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.ParameterException
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.util.NotificationConstants
import rahulstech.android.expensetracker.backuprestore.util.createRestoreNotification
import rahulstech.android.expensetracker.backuprestore.worker.job.JsonRestoreJob
import rahulstech.android.expensetracker.domain.ExpenseRepository
import java.io.File
import java.io.InputStream

class JsonRestoreWorker(context: Context, parameters: WorkerParameters): Worker(context, parameters) {

    companion object {
        private val TAG = JsonRestoreWorker::class.simpleName
    }

    private val repo = ExpenseRepository.getInstance(context)

    private var job: JsonRestoreJob? = null

    override fun doWork(): Result {
        startForeground()
        var inputFile: File? = null
        try {
            inputFile = getInputFile()

            job = JsonRestoreJob.create(
                sourceFactory = { openInputBackupFile(inputFile) },
                repo = repo.restoreRepository
            )
            job?.restore()
            return Result.success()
        }
        catch (ex: Exception) {
            Log.e(TAG,"JsonRestoreWorker failed with exception",ex)
            return Result.failure()
        }
        finally {
            runCatching { inputFile?.deleteRecursively() }
        }
    }

    override fun onStopped() {
        super.onStopped()
        job?.terminate()
    }

    internal fun restore() {}

    private fun startForeground() {
        val message = applicationContext.getString(R.string.notification_message_restore)
        val notification = createRestoreNotification(applicationContext,message)
        val foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(NotificationConstants.RESTORE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NotificationConstants.RESTORE_NOTIFICATION_ID, notification)
        }
        setForegroundAsync(foregroundInfo)
    }

    private fun getInputVersion(): Int {
        if (!inputData.hasKeyWithValueOfType(Constants.DATA_VERSION,Int::class.java)) {
            throw ParameterException("inputData \"version\" not found")
        }
        return inputData.getInt(Constants.DATA_VERSION, 0)
    }
    private fun getInputFile(): File {
        val filePath = inputData.getString(Constants.DATA_FILE_PATH)
        if (filePath.isNullOrBlank()) {
            throw ParameterException("inputData \"file_path\" not found")
        }
        return File(filePath)
    }

    private fun openInputBackupFile(file: File): InputStream =
        file.inputStream()

    private fun deleteJsonFileSilently(jsonFile: File) {

    }
}