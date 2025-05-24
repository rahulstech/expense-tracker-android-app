package rahulstech.android.expensetracker.backuprestore.worker.backup

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper

class BackupWorker(context: Context, parameters: WorkerParameters): Worker(context,parameters) {

    override fun doWork(): Result {
        BackupRestoreHelper.startBackupOnce(applicationContext)
        return Result.success()
    }
}