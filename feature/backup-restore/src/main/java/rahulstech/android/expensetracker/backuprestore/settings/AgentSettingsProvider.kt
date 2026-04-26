package rahulstech.android.expensetracker.backuprestore.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import rahulstech.android.expensetracker.backuprestore.R
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

enum class BackupFrequency {
    NEVER,
    DAILY,
    WEEKLY,
    ;

    fun getLabel(context: Context): String {
        return when(this) {
            NEVER -> context.getString(R.string.backup_frequency_never)
            DAILY -> context.getString(R.string.backup_frequency_daily)
            WEEKLY -> context.getString(R.string.backup_frequency_weekly)
        }
    }
}

@Singleton
class AgentSettingsProvider @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context
){
    companion object {

        private const val TAG = "AgentSettingsProvider"
        private const val SHARED_PREFERENCES_NAME = "rahulstech.android.expensetrcker.backuprestore.settings.agent"
        private const val KEY_BACKUP_FREQUENCY = "backup_frequency"
        private const val KEY_LAST_LOCAL_BACKUP_DATETIME = "last_local_backup_datetime"
    }

    private val sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _backupFrequencyFlow = MutableStateFlow(getBackupFrequency())

    private val _lastLocalBackupDateTimeFlow = MutableStateFlow(getLastLocalBackupLocalDateTime())

    private val sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when(key) {
            KEY_BACKUP_FREQUENCY -> _backupFrequencyFlow.value = getBackupFrequency()
            KEY_LAST_LOCAL_BACKUP_DATETIME -> _lastLocalBackupDateTimeFlow.value = getLastLocalBackupLocalDateTime()
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    //------------- Backup Frequency ----------------------

    fun setBackupFrequency(frequency: BackupFrequency) {
        edit {
            putString(KEY_BACKUP_FREQUENCY, frequency.name)
        }
    }

    fun getBackupFrequency(): BackupFrequency {
        val name = sharedPreferences.getString(KEY_BACKUP_FREQUENCY, null)
        return name?.let{ BackupFrequency.valueOf(name) } ?: BackupFrequency.NEVER
    }

    fun getBackupFrequencyFlow(): Flow<BackupFrequency> = _backupFrequencyFlow

    //------------- Last Local Backup Date Time ----------------------

    fun setLastLocalBackupNow() {
        edit {
            putString(KEY_LAST_LOCAL_BACKUP_DATETIME, LocalDateTime.now().toString())
        }
    }

    fun getLastLocalBackupLocalDateTime(): LocalDateTime? =
        sharedPreferences.getString(KEY_LAST_LOCAL_BACKUP_DATETIME, null)
            ?.let { value -> LocalDateTime.parse(value) }

    fun getLastLocalBackupLocalDateTimeFlow(): Flow<LocalDateTime?> = _lastLocalBackupDateTimeFlow

    //------------- Helpers ----------------------

    private fun edit(action: SharedPreferences.Editor.()-> Unit) {
        sharedPreferences.edit(true,action)
    }
}