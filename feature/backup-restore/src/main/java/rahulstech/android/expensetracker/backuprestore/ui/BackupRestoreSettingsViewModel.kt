package rahulstech.android.expensetracker.backuprestore.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.backuprestore.util.FileEntry
import rahulstech.android.expensetracker.backuprestore.util.FileUtil

class BackupRestoreSettingsViewModel(app: Application): AndroidViewModel(app) {

    companion object {
        private val TAG = BackupRestoreSettingsViewModel::class.simpleName
    }

    private val applicationContext = getApplication<Application>().applicationContext

    private lateinit var backupFilesFlow: MutableStateFlow<List<FileEntry>>

    fun getBackupFiles(): Flow<List<FileEntry>> {
        if (!::backupFilesFlow.isInitialized) {
            backupFilesFlow = MutableStateFlow(emptyList())
            viewModelScope.launch {
                FileUtil.getBackupFilesFlow(applicationContext, 5)
                    .collectLatest { entries ->
                        backupFilesFlow.value =  entries
                    }
            }
        }
        return backupFilesFlow
    }
}