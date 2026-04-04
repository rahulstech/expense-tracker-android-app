package rahulstech.android.expensetracker.backuprestore.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper
import rahulstech.android.expensetracker.backuprestore.worker.ProgressData
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context,
    private val agentSettingsProvider: AgentSettingsProvider,
): ViewModel() {

    companion object {
        private const val TAG = "BackupRestoreViewModel"
    }

    private var backupProgress: Flow<ProgressData?>? = null

    private var restoreProgress: Flow<ProgressData?>? = null

    fun getBackupProgressFlow(): Flow<ProgressData?> {
        if (null == backupProgress) {
            backupProgress = BackupRestoreHelper.getBackupProgress(applicationContext)
        }
        return backupProgress!!
    }

    fun getRestoreProgressFlow(): Flow<ProgressData?> {
        if (null == restoreProgress) {
            restoreProgress = BackupRestoreHelper.getRestoreProgress(applicationContext)
        }
        return restoreProgress!!
    }

    private var lastLocalBackupTime: LiveData<LocalDateTime>? = null

    fun getLastLocalBackupTime(): LiveData<LocalDateTime> {
        if (null == lastLocalBackupTime) {
            lastLocalBackupTime = agentSettingsProvider.getLastLocalBackupLocalDateTimeLiveData()
        }
        return lastLocalBackupTime!!
    }
}