package rahulstech.android.expensetracker.backuprestore.settings

import android.content.Context
import androidx.core.content.edit

enum class BackupFrequency {
    NEVER,
    WEEKLY,
    MONTHLY,
    ;
}

class AgentSettingsProvider private constructor(val applicationContext: Context){

    private val sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun backup(): AgentSettingsModel {
        val frequency = getBackupFrequency()

        return AgentSettingsModel(frequency)
    }

    fun restore(data: AgentSettingsModel) {
        setBackupFrequency(data.backupFrequency)
    }

    fun getBackupFrequency(): BackupFrequency {
        val name = sharedPreferences.getString(KEY_BACKUP_FREQUENCY, null)
        return name?.let{ BackupFrequency.valueOf(name) } ?: BackupFrequency.NEVER
    }

    fun setBackupFrequency(frequency: BackupFrequency) {
        sharedPreferences.edit(true) {
            putString(KEY_BACKUP_FREQUENCY, frequency.name)
        }
    }

    companion object {

        private const val SHARED_PREFERENCES_NAME = "rahulstech.android.expensetrcker.backuprestore.settings.agent"
        private const val KEY_BACKUP_FREQUENCY = "rahulstech.android.expensetracker.backuprestore.BACKUP_FREQUENCY"

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