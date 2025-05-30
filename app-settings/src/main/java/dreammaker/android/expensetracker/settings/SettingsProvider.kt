package dreammaker.android.expensetracker.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData

enum class ViewHistory {
    MONTHLY,
    DAILY,
    ;

    fun getLabel(context: Context): CharSequence
    = context.getString(when(this) {
        MONTHLY -> R.string.label_history_list_view_monthly
        DAILY -> R.string.label_history_list_view_daily
    })
}

class SettingsProvider private constructor(appContext: Context) {

    private val store: SharedPreferences = appContext.getSharedPreferences(NAME, MODE)

    fun backup(): SettingsModel {
        return SettingsModel()
    }

    fun restore(model: SettingsModel) {
        // TODO: implement restore SettingsProvider
    }

    fun getViewHistory(): ViewHistory {
        val value = store.getString(KEY_VIW_HISTORY, ViewHistory.DAILY.name)!!
        return ViewHistory.valueOf(value)
    }

    fun setViewHistory(view: ViewHistory) {
        store.edit(true) {
            putString(KEY_VIW_HISTORY, view.name)
        }
    }

    fun getSortHistoryDateAscending(): Boolean {
        return store.getBoolean(KEY_SORT_HISTORY_DATE_ASC, true)
    }

    fun getSortHistoryDateAscendingLiveData(): LiveData<Boolean> = SharedPreferenceLiveData(KEY_SORT_HISTORY_DATE_ASC)

    fun setSortHistoryDateAscending(value: Boolean) {
        store.edit(true) {
            putBoolean(KEY_SORT_HISTORY_DATE_ASC, value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getValue(key: String): T? {
        val value = when(key) {
            KEY_VIW_HISTORY -> getViewHistory()
            KEY_SORT_HISTORY_DATE_ASC -> getSortHistoryDateAscending()
            else -> throw IllegalStateException("unknown key $key") // should never reach this branch
        }
        return value as T
    }

    companion object {
        const val NAME = "dreammaker.android.expensetracker.settings.APP_SETTINGS"
        const val MODE = Context.MODE_PRIVATE
        private const val KEY_VIW_HISTORY = "view_history"
        private const val KEY_SORT_HISTORY_DATE_ASC = "sort_history_date_asc"

        @Volatile
        private var instance: SettingsProvider? = null

        fun get(context: Context): SettingsProvider {
            return instance ?: synchronized(this) {
                val provider = SettingsProvider(context.applicationContext)
                instance = provider
                provider
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
            store.registerOnSharedPreferenceChangeListener(sharedPreferenceCallback)
        }

        override fun onInactive() {
            store.unregisterOnSharedPreferenceChangeListener(sharedPreferenceCallback)
        }
    }
}