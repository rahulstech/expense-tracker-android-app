package rahulstech.android.expensetracker.backuprestore.settings

import android.content.Context
import androidx.core.content.edit
import rahulstech.android.expensetracker.backuprestore.R

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

class AgentSettingsProvider private constructor(private val applicationContext: Context){

    private val sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getBackupFrequency(): BackupFrequency {
        val name = sharedPreferences.getString(KEY_BACKUP_FREQUENCY, null)
        return name?.let{ BackupFrequency.valueOf(name) } ?: BackupFrequency.NEVER
    }

    fun setBackupFrequency(frequency: BackupFrequency) {
        sharedPreferences.edit(true) {
            putString(KEY_BACKUP_FREQUENCY, frequency.name)
        }
    }

    fun setLastLocalBackupMillis(millis: Long) {
        sharedPreferences.edit(true) {
            putLong(KEY_LAST_LOCAL_BACKUP_MILLIS, millis)
        }
    }

    fun getLastLocalBackupMillis(): Long = sharedPreferences.getLong(KEY_LAST_LOCAL_BACKUP_MILLIS, -1)

    companion object {

        private const val SHARED_PREFERENCES_NAME = "rahulstech.android.expensetrcker.backuprestore.settings.agent"
        private const val KEY_BACKUP_FREQUENCY = "backup_frequency"
        private const val KEY_LAST_LOCAL_BACKUP_MILLIS = "last_local_backup_millis"

        @Volatile
        private var instance: AgentSettingsProvider? = null

        fun get(context: Context): AgentSettingsProvider {
            return instance ?: synchronized(this) {
                val tmp = AgentSettingsProvider(context.applicationContext)
                instance = tmp
                tmp
            }
        }
    }
}