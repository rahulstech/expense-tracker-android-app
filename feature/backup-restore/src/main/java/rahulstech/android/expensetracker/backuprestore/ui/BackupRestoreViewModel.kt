package rahulstech.android.expensetracker.backuprestore.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper
import rahulstech.android.expensetracker.backuprestore.worker.ProgressData

class BackupRestoreViewModel(private val app: Application): AndroidViewModel(app) {

    companion object {
        private const val TAG = "BackupRestoreViewModel"
    }

    private var backupProgress: Flow<ProgressData?>? = null

    private var restoreProgress: Flow<ProgressData?>? = null

    fun getBackupProgressFlow(): Flow<ProgressData?> {
        if (null == backupProgress) {
            backupProgress = BackupRestoreHelper.getBackupProgress(app.applicationContext)
        }
        return backupProgress!!
    }

    fun getRestoreProgressFlow(): Flow<ProgressData?> {
        if (null == restoreProgress) {
            restoreProgress = BackupRestoreHelper.getRestoreProgress(app.applicationContext)
        }
        return restoreProgress!!
    }

    private var lastLocalBackupTime: LiveData<Long>? = null

    fun getLastLocalBackupTime(): LiveData<Long> {
        if (null == lastLocalBackupTime) {
            lastLocalBackupTime = AgentSettingsProvider.get(app.applicationContext).getLastLocalBackupMillisLiveData()
        }
        return lastLocalBackupTime!!
    }
}