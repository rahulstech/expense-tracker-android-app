package rahulstech.android.expensetracker.backuprestore.worker.restore

import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.util.FileUtil
import rahulstech.android.expensetracker.backuprestore.util.NotificationConstants
import rahulstech.android.expensetracker.backuprestore.util.createRestoreNotification
import java.io.File

class GZipRestoreWorker(context: Context, parameters: WorkerParameters): Worker(context,parameters) {

    companion object {
        private val TAG = GZipRestoreWorker::class.simpleName
    }

    override fun doWork(): Result {
        startForeground()
        try {
            val inputBackupFile = getInputBackupFile()
            val resultData = restore(inputBackupFile)
            return Result.success(resultData)
        }
        catch (ex: Exception) {
            Log.e(TAG, "GZipRestoreWorker failed with exception",ex)
            return Result.failure()
        }
    }

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

    internal fun restore(inputBackupFile: Uri): Data =
        FileUtil.openInputStream(applicationContext, inputBackupFile).use {
            val gzIS = GzipCompressorInputStream(it)
            val tarIS = TarArchiveInputStream(gzIS)
            val version = readVersion(tarIS)
            val file = restoreFromArchive(version,tarIS)
            Log.i(TAG,"restored \"$inputBackupFile\" to \"$file\", version=$version")
            workDataOf(
                Constants.DATA_VERSION to version,
                Constants.DATA_FILE_PATH to file.absolutePath
            )
        }

    internal fun readVersion(tarIS: TarArchiveInputStream): Int {
        val entry = tarIS.nextEntry
        val content = ByteArray(entry.size.toInt())
        tarIS.read(content)
        val version = content.toString(Charsets.UTF_8).toInt()
        return version
    }

    internal fun restoreFromArchive(version: Int, input: TarArchiveInputStream): File {
        val file = copyFile(input, applicationContext.cacheDir)
        return file
    }

    internal fun copyFile(tarIS: TarArchiveInputStream, outputDir: File): File {
        val entry = tarIS.nextEntry
        val outputFile = File(outputDir, entry.name)
        outputFile.outputStream().use {
            FileUtil.copy(tarIS, it)
        }
        return outputFile
    }

    private fun getInputBackupFile(): Uri {
        val path = inputData.getString(Constants.DATA_BACKUP_FILE)
            ?: throw IllegalStateException("no input data found for name ${Constants.DATA_BACKUP_FILE}")
        return path.toUri()
    }
}