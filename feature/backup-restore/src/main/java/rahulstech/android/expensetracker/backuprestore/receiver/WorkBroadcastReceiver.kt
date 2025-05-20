package rahulstech.android.expensetracker.backuprestore.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper
import rahulstech.android.expensetracker.backuprestore.worker.Constants

class WorkBroadcastReceiver: BroadcastReceiver() {

    companion object {
        const val ACTION_CANCEL_WORK = "rahulstecch.android.expensetracker.action.CANCEL_WORK"
        const val EXTRA_WORK_NAME = "rahulstech.android.expensetracker.extra.WORK_NAME"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val action = intent.action
        if (action == ACTION_CANCEL_WORK) {
            cancelWork(appContext, intent)
        }
    }

    private fun cancelWork(appContext: Context, intent: Intent) {
        val workName = intent.getStringExtra(EXTRA_WORK_NAME)
        if (workName == Constants.TAG_BACKUP_WORK) {
            BackupRestoreHelper.cancelBackup(appContext)
        }
    }
}