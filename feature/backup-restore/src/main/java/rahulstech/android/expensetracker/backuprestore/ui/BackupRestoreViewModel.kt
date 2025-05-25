package rahulstech.android.expensetracker.backuprestore.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper

class BackupRestoreViewModel(app: Application): AndroidViewModel(app) {

    companion object {
        private val TAG = BackupRestoreViewModel::class.simpleName
    }

    private val applicationContext = getApplication<Application>().applicationContext

    private var backupProgress: Flow<BackupRestoreHelper.ProgressData?>? = null

    private var restoreProgress: Flow<BackupRestoreHelper.ProgressData?>? = null

    fun getBackupProgressFlow(): Flow<BackupRestoreHelper.ProgressData?> {
        if (null == backupProgress) {
            backupProgress = BackupRestoreHelper.getBackupProgress(applicationContext)
        }
        return backupProgress!!
    }

    fun getRestoreProgressFlow(): Flow<BackupRestoreHelper.ProgressData?> {
        if (null == restoreProgress) {
            restoreProgress = BackupRestoreHelper.getRestoreProgress(applicationContext)
        }
        return restoreProgress!!
    }

    private var lastLocalBackupTime: LiveData<Long>? = null

    fun getLastLocalBackupTime(): LiveData<Long> {
        if (null == lastLocalBackupTime) {
            lastLocalBackupTime = AgentSettingsProvider.get(applicationContext).getLastLocalBackupMillisLiveData()
        }
        return lastLocalBackupTime!!
    }
}