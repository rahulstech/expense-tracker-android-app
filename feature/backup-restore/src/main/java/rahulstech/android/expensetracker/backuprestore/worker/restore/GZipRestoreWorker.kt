package rahulstech.android.expensetracker.backuprestore.worker.restore

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.util.FileUtil
import rahulstech.android.expensetracker.backuprestore.util.NotificationBuilder
import rahulstech.android.expensetracker.backuprestore.util.NotificationConstants
import rahulstech.android.expensetracker.backuprestore.Constants
import java.io.File
import java.io.InputStream

class GZipRestoreWorker(context: Context, parameters: WorkerParameters): Worker(context,parameters) {

    companion object {
        private val TAG = GZipRestoreWorker::class.simpleName
    }

    override fun doWork(): Result {
        setForegroundAsync(createForegroundInfo(createRestoreNotification()))

        val inputBackupFile = getInputBackupFile()
        val outputBackupFile: File
        try {
            outputBackupFile = restore(inputBackupFile)
            Log.i(TAG,"outputBackupFile=$outputBackupFile")
        }
        catch (ex: Exception) {
            Log.e(TAG, "GZipRestoreWorker failed with exception",ex)
            return Result.failure()
        }

        val resultData = workDataOf(
            Constants.DATA_BACKUP_FILE to Uri.fromFile(outputBackupFile).toString()
        )
        return Result.success(resultData)
    }

    private fun restore(inputBackupFile: Uri): File {
        var source: InputStream? = null
        var gzIS: GzipCompressorInputStream? = null
        var tarIS: TarArchiveInputStream? = null
        try {
            source = FileUtil.openInputStream(applicationContext, inputBackupFile)
            gzIS = GzipCompressorInputStream(source)
            tarIS = TarArchiveInputStream(gzIS)

            readVersion(tarIS)
            val backupFile = readBackupFile(tarIS, applicationContext.cacheDir)
            return backupFile
        }
        finally {
            runCatching { tarIS?.close() }
            runCatching { gzIS?.close() }
            runCatching { source?.close() }
        }
    }

    private fun readVersion(tarIS: TarArchiveInputStream): Int {
        val entry = tarIS.nextEntry
        val content = ByteArray(entry.size.toInt())
        tarIS.read(content)
        val version = content.toString(Charsets.UTF_8).toInt()
        return version
    }

    private fun readBackupFile(tarIS: TarArchiveInputStream, outputDir: File): File {
        val entry = tarIS.nextEntry
        val outputFile = File(outputDir, entry.name)
        val dest = outputFile.outputStream()
        dest.use { FileUtil.copy(tarIS, dest) }
        return outputFile
    }

    private fun getInputBackupFile(): Uri {
        val path = inputData.getString(Constants.DATA_BACKUP_FILE)
            ?: throw IllegalStateException("no input data found for name ${Constants.DATA_BACKUP_FILE}")
        return Uri.parse(path)
    }

    private fun createRestoreNotification(): Notification {
        val builder = NotificationBuilder().apply {
            setMessage(getStyledMessage())
        }
        val progressData = workDataOf(
            Constants.DATA_PROGRESS_MESSAGE to getPlainMessage()
        )
        setProgressAsync(progressData)
        return rahulstech.android.expensetracker.backuprestore.util.createRestoreNotification(
            applicationContext,
            builder
        )
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

}