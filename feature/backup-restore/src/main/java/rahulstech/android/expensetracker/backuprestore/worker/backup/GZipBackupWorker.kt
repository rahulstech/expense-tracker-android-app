package rahulstech.android.expensetracker.backuprestore.worker.backup

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.receiver.WorkBroadcastReceiver
import rahulstech.android.expensetracker.backuprestore.util.DateTimeUtil
import rahulstech.android.expensetracker.backuprestore.util.FileUtil
import rahulstech.android.expensetracker.backuprestore.util.NotificationBuilder
import rahulstech.android.expensetracker.backuprestore.util.NotificationConstants
import rahulstech.android.expensetracker.backuprestore.util.createBackupNotification
import rahulstech.android.expensetracker.backuprestore.worker.Constants
import java.io.File
import java.util.Locale

class GZipBackupWorker(context: Context, parameters: WorkerParameters): Worker(context,parameters) {

    companion object {
        private val TAG = GZipBackupWorker::class.simpleName
        private const val CURRENT_VERSION = 1
        private const val PRIVATE_BACKUP_FILE_NAME = "backup.tar.gz"
        private const val PUBLIC_BACKUP_FILE_NAME = "backup_%s.tar.gz"
        private const val NAME_VERSION = "VERSION"
        private const val NAME_BACKUP_DATA = "backup.json"
        private const val MAX_PROGRESS = 2
    }

    override fun doWork(): Result {
        val inputBackupFile = getInputBackupFile()
        val backupFile: File

        updateProgress(0, R.string.message_backup_progress)
        try {
            // start gzip backup
            backupFile = backup(inputBackupFile)

            // copy archive file to public directory
            copyBackupFileToExternalPublicDirectory(backupFile)

            // update last backup time
            notifyBackupTime()

            // delete unnecessary files
            runCatching { inputBackupFile.delete() }
        }
        catch (ex: Exception) {
            Log.e(TAG, "GZipBackupWork failed with exception", ex)
            return Result.failure()
        }

        val resultData = workDataOf(
            Constants.DATA_BACKUP_FILE to backupFile.canonicalPath
        )
        return Result.success(resultData)
    }

    fun backup(inputBackupFile: File): File {
        Log.i(TAG, "inputBackupFile=$inputBackupFile")

        val privateBackupFile = getPrivateBackupFile()
        val gzOS = GzipCompressorOutputStream(privateBackupFile.outputStream())
        val tarOS = TarArchiveOutputStream(gzOS)

        try {
            throwIfStopped()
            writeVersion(tarOS, CURRENT_VERSION)

            // add backup.json file
            throwIfStopped()
            writeBackupFile(tarOS, inputBackupFile)

            updateProgress(1, R.string.message_backup_progress_archiving)
        }
        finally {
            runCatching { tarOS.close() }
            runCatching { gzOS.close() }
        }

        return privateBackupFile
    }

    private fun writeVersion(tarOS: TarArchiveOutputStream, version: Int) {
        val bytes = version.toString().toByteArray()
        writeByteArrayEntry(tarOS, NAME_VERSION, bytes)
    }

    private fun writeBackupFile(tarOS: TarArchiveOutputStream, backupFile: File) {
        writeFileEntry(tarOS, NAME_BACKUP_DATA, backupFile)
    }

    @VisibleForTesting
    fun writeFileEntry(tarOS: TarArchiveOutputStream, name: String, content: File) {
        val entry = TarArchiveEntry(name)
        entry.size = content.length()
        tarOS.putArchiveEntry(entry)
        content.inputStream().use { FileUtil.copy(it, tarOS) }
        tarOS.closeArchiveEntry()
    }

    @VisibleForTesting
    fun writeByteArrayEntry(tarOS: TarArchiveOutputStream, name: String, bytes: ByteArray) {
        val entry = TarArchiveEntry(name)
        entry.size = bytes.size.toLong()
        tarOS.putArchiveEntry(entry)
        tarOS.write(bytes)
        tarOS.flush()
        tarOS.closeArchiveEntry()
    }

    private fun getInputBackupFile(): File {
        val path = inputData.getString(Constants.DATA_BACKUP_FILE)
            ?: throw IllegalStateException("no input data found for name ${Constants.DATA_BACKUP_FILE}")
        return File(path)
    }

    private fun getPrivateBackupFile(): File {
        val dir = applicationContext.cacheDir
        val backupFile = File(dir, PRIVATE_BACKUP_FILE_NAME)
        return backupFile
    }

    private fun getPublicBackupFilename(): String {
        val currentTimeStamp = DateTimeUtil.formatDateTimeInFilename()
        val filename = String.format(Locale.ENGLISH, PUBLIC_BACKUP_FILE_NAME, currentTimeStamp)
        return filename
    }

    private fun copyBackupFileToExternalPublicDirectory(privateBackupFile: File) {
        throwIfStopped()
        val publicBackupFileName = getPublicBackupFilename()
        val output = FileUtil.openPublicBackupFileOutputStream(applicationContext, publicBackupFileName)
        output.use { dest ->
            privateBackupFile.inputStream().use { src ->
                Log.i(TAG, "coping from $privateBackupFile to $publicBackupFileName in external public storage")
                FileUtil.copy(src,dest)
                updateProgress(2, R.string.message_backup_progress_copying)
            }
        }
    }

    private fun notifyBackupTime() {
        applicationContext.sendBroadcast(Intent(applicationContext, WorkBroadcastReceiver::class.java).apply {
            action = WorkBroadcastReceiver.ACTION_UPDATE_LAST_BACKUP_MILLIS
            putExtra(WorkBroadcastReceiver.EXTRA_LOCAL_BACKUP, DateTimeUtil.currentTimeMillis())
        })
    }

    private fun updateProgress(current: Int, @StringRes messageId: Int) {
        val message = applicationContext.getString(messageId)
        setProgressAsync(
            workDataOf(
                Constants.DATA_PROGRESS_MAX to MAX_PROGRESS,
                Constants.DATA_PROGRESS_CURRENT to current,
                Constants.DATA_PROGRESS_MESSAGE to message
            )
        )
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
}