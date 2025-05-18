package rahulstech.android.expensetracker.backuprestore.worker.restore

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class JsonRestoreWork(context: Context, parameters: WorkerParameters): Worker(context,parameters) {

    override fun doWork(): Result {
        TODO("Not yet implemented")
    }
}