package rahulstech.android.expensetracker.backuprestore.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.backuprestore.util.FileEntry
import rahulstech.android.expensetracker.backuprestore.util.FileUtil
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper

class BackupRestoreSettingsViewModel(app: Application): AndroidViewModel(app) {

    companion object {
        private val TAG = BackupRestoreSettingsViewModel::class.simpleName
    }

    private val applicationContext = getApplication<Application>().applicationContext

    private var backupFilesFlow: Flow<List<FileEntry>>? = null

    private var backupProgress: Flow<BackupRestoreHelper.ProgressData?>? = null

    private var restoreProgress: Flow<BackupRestoreHelper.ProgressData?>? = null

    suspend fun getBackupFiles(): Flow<List<FileEntry>> {
        if (null == backupFilesFlow) {
            backupFilesFlow = FileUtil.getBackupFilesFlow(applicationContext, 1)
        }
        return backupFilesFlow!!
    }

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
}