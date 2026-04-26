package rahulstech.android.expensetracker.backuprestore.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.settings.BackupFrequency
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper
import java.time.LocalDateTime
import javax.inject.Inject


data class BackupRestoreActivityUIState(
    val lastLocalBackupTime: LocalDateTime? = null,
    val backupFrequency: BackupFrequency = BackupFrequency.NEVER,
)


@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context,
    private val agentSettingsProvider: AgentSettingsProvider,
): ViewModel() {

    companion object {
        private const val TAG = "BackupRestoreViewModel"
    }

    val uiState = combine(
        agentSettingsProvider.getLastLocalBackupLocalDateTimeFlow(),
        agentSettingsProvider.getBackupFrequencyFlow()
    ){ datetime, frequency ->
        BackupRestoreActivityUIState(
            lastLocalBackupTime = datetime,
            backupFrequency = frequency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BackupRestoreActivityUIState()
    )

    fun changeBackupFrequency(newFrequency: BackupFrequency) {
        BackupRestoreHelper.rescheduleBackup(applicationContext, newFrequency)
        agentSettingsProvider.setBackupFrequency(newFrequency)
    }
}