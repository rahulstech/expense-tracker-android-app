package rahulstech.android.expensetracker.backuprestore.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper

class WorkBroadcastReceiver: BroadcastReceiver() {

    companion object {
        const val ACTION_CANCEL_WORK = "rahulstecch.android.expensetracker.action.CANCEL_WORK"
        const val ACTION_UPDATE_LAST_BACKUP_MILLIS = "rahulstecch.android.expensetracker.action.UPDATE_LAST_BACKUP_MILLIS"
        const val EXTRA_WORK_NAME = "extra_work_name"
        const val EXTRA_LOCAL_BACKUP = "extra_local_backup"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val action = intent.action
        when(action) {
            ACTION_CANCEL_WORK -> cancelWork(appContext, intent)
            ACTION_UPDATE_LAST_BACKUP_MILLIS -> updateLastBackupMillis(appContext, intent)
        }
    }

    private fun cancelWork(appContext: Context, intent: Intent) {
        val workName = intent.getStringExtra(EXTRA_WORK_NAME)
        if (workName == Constants.TAG_BACKUP_WORK) {
            BackupRestoreHelper.cancelBackup(appContext)
        }
    }

    private fun updateLastBackupMillis(appContext: Context, intent: Intent) {
        AgentSettingsProvider.get(appContext).setLastLocalBackupNow()
    }
}