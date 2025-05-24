package rahulstech.android.expensetracker.backuprestore.worker.restore

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class GZipRestoreWorker(context: Context, parameters: WorkerParameters): Worker(context,parameters) {

    override fun doWork(): Result {

        // get the gzip file

        // open gzip file for reading

        // read entries

        // return the backup.json file path

        // clean up


        return Result.success()
    }
}