package rahulstech.android.expensetracker.backuprestore.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture

class LocalBackupWorker(context: Context, params: WorkerParameters): ListenableWorker(context,params) {

    override fun startWork(): ListenableFuture<Result> {
        TODO()
    }
}