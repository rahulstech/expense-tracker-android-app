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

    fun isFirstRestoreAsked(): Boolean = store.getBoolean(KEY_FIRST_RESTORE_ASKED, false)

    fun markFirstRestoreAsked() {
        store.edit(true) {
            putBoolean(KEY_FIRST_RESTORE_ASKED, true)
        }
    }

    companion object {

        private const val KEY_VIW_HISTORY = "key.view_history"
        private const val KEY_FIRST_RESTORE_ASKED: String = "first_restore_asked"

        private lateinit var instance: SettingsProvider

        fun get(context: Context): SettingsProvider {
            if (!::instance.isInitialized) {
                instance = SettingsProvider(context.applicationContext)
            }
            return instance
        }
    }
}