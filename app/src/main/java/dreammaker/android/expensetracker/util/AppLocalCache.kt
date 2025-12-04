package dreammaker.android.expensetracker.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

enum class ViewHistory {
    DAILY,
    MONTHLY,
    ;
}

enum class SortByDate {
    OLD_FIRST,
    NEW_FIRST,
    ;
}

class AppLocalCache(private val context: Context) {

    companion object {
        private const val SHARED_PREFERENCE_NAME = "dreammaker.android.expensetracker.preferernce.applocalcache"

        private const val KEY_VIEW_HISTORY = "view_history"
        private const val KEY_SORT_HISTORY_BY_DATE = "sort_history_by_date"
        private const val KEY_SHOW_SET_DEFAULT_ACCOUNT = "show_set_default_account"
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

    fun setSortHistoryByDate(value: SortByDate) {
        edit {
            putString(KEY_SORT_HISTORY_BY_DATE,value.name)
        }
    }

    fun getSortHistoryByDate(defaultValue: SortByDate = SortByDate.OLD_FIRST): SortByDate =
        sp.getString(KEY_SORT_HISTORY_BY_DATE,null)
            ?.let { name -> SortByDate.valueOf(name) }
            ?: defaultValue


    fun isShowSetDefaultAccount(): Boolean = sp.getBoolean(KEY_SHOW_SET_DEFAULT_ACCOUNT, true)

    fun setShowSetDefaultAccount(show: Boolean) {
        edit {
            putBoolean(KEY_SHOW_SET_DEFAULT_ACCOUNT,show)
        }
    }

    private fun edit(action: SharedPreferences.Editor.()->Unit) {
        sp.edit(true,action)
    }
}