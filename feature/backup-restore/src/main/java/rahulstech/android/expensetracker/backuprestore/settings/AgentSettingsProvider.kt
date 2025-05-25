package rahulstech.android.expensetracker.backuprestore.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
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

class AgentSettingsProvider private constructor(applicationContext: Context){

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

    fun getLastLocalBackupMillisLiveData(): LiveData<Long> = SharedPreferenceLiveData(KEY_LAST_LOCAL_BACKUP_MILLIS)

    @Suppress("UNCHECKED_CAST")
    private fun <T> getValue(key: String): T? {
        val value = when(key) {
            KEY_BACKUP_FREQUENCY -> getBackupFrequency()
            KEY_LAST_LOCAL_BACKUP_MILLIS -> getLastLocalBackupMillis()
            else -> throw IllegalStateException("unknown key $key") // should never reach this branch
        }
        return value as T
    }

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

    private inner class SharedPreferenceLiveData<T>(val observeKey: String): LiveData<T>() {


        private val sharedPreferenceCallback: SharedPreferences.OnSharedPreferenceChangeListener
        = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (observeKey == key) {
                val value = getValue<T>(key)
                postValue(value)
            }
        }

        init {
            value = getValue(observeKey)
        }

        override fun onActive() {
            sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceCallback)
        }

        override fun onInactive() {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceCallback)
        }
    }
}