package rahulstech.android.expensetracker.backuprestore.worker.backup

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class GZipBackupWork(context: Context, parameters: WorkerParameters): Worker(context,parameters) {

    override fun doWork(): Result {

        // create VERSION text file

        // add VERSION and backup file, created during last work, in gzip entries

        // create tar gzip archive file

        // copy archive file to public directory


        return Result.success()
    }
}