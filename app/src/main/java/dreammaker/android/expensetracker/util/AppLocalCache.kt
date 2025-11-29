package dreammaker.android.expensetracker.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

enum class ViewHistory {
    DAILY,
    MONTHLY,
    ;
}

class AppLocalCache(private val context: Context) {

    companion object {
        private const val SHARED_PREFERENCE_NAME = "dreammaker.android.expensetracker.preferernce.applocalcache"

        private const val KEY_VIEW_HISTORY = "view_history"
    }


    private val sp by lazy { context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE) }

    fun setViewHistory(value: ViewHistory) {
        edit {
            putString(KEY_VIEW_HISTORY,value.name)
        }
    }

    fun getViewHistory(defaultValue: ViewHistory = ViewHistory.DAILY): ViewHistory =
        sp.getString(KEY_VIEW_HISTORY,null)
            ?.let { name -> ViewHistory.valueOf(name) }
            ?: defaultValue

    private fun edit(action: SharedPreferences.Editor.()->Unit) {
        sp.edit(true,action)
    }
}