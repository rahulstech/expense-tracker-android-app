package dreammaker.android.expensetracker.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dreammaker.android.expensetracker.R

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

class SettingsProvider private constructor(private val context: Context) {

    private val store: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getViewHistory(): ViewHistory {
        val value = store.getString(KEY_VIW_HISTORY, ViewHistory.DAILY.name)!!
        return ViewHistory.valueOf(value)
    }

    fun setViewHistory(view: ViewHistory) {
        store.edit(true) {
            putString(KEY_VIW_HISTORY, view.name)
        }
    }

    companion object {

        private const val KEY_VIW_HISTORY = "settings_provider.key.view_history"

        private lateinit var instance: SettingsProvider

        fun get(context: Context): SettingsProvider {
            if (!::instance.isInitialized) {
                instance = SettingsProvider(context.applicationContext)
            }
            return instance
        }
    }
}