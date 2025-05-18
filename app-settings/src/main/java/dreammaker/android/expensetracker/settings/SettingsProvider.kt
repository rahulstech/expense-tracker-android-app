package dreammaker.android.expensetracker.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

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

class SettingsProvider private constructor(context: Context) {

    private val store: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    fun backup(): SettingsModel {
        val viewHistory = getViewHistory()

        return SettingsModel(viewHistory)
    }

    fun restore(model: SettingsModel) {

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

    fun isFirstRestoreAsked(): Boolean = store.getBoolean(KEY_FIRST_RESTORE_ASKED, false)

    fun markFirstRestoreAsked() {
        store.edit(true) {
            putBoolean(KEY_FIRST_RESTORE_ASKED, true)
        }
    }

    companion object {

        private const val KEY_VIW_HISTORY = "dreammaker.android.expensetracker.settings.VIEW_HISTORY"
        private const val KEY_FIRST_RESTORE_ASKED: String = "dreammaker.android.expensetracker.settings.FIRST_RESTORE_ASKED"

        @Volatile
        private var instance: SettingsProvider? = null

        fun get(context: Context): SettingsProvider {
            return instance ?: synchronized(this) {
                val provider = SettingsProvider(context)
                instance = provider
                provider
            }
        }
    }
}