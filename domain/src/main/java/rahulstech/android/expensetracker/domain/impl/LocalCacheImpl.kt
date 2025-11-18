package rahulstech.android.expensetracker.domain.impl


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import rahulstech.android.expensetracker.domain.LocalCache
import java.time.LocalDateTime

internal class LocalCacheImpl(
    context: Context
): LocalCache {
    companion object {
        private const val SP_NAME = "rahulstech.android.expensetracker.domain.LocalCache"
        private const val PREFIX_ACCOUNT = "account"
        private const val PREFIX_GROUP = "group"
        private const val KEY_TOTAL_USED = "total_used"
    }

    private val sp: SharedPreferences = context.applicationContext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE)

    override fun getAccountTotalUsed(id: Long,defaultValue: Long): Long = sp.getLong(getAccountKey(id,KEY_TOTAL_USED),defaultValue)

    override fun setAccountTotalUsed(id: Long,value: Long) {
        edit {
            putLong(getAccountKey(id,KEY_TOTAL_USED),value)
        }
    }

    override fun removeAccountTotalUsed(id: Long) {
        edit {
            remove(getAccountKey(id,KEY_TOTAL_USED))
        }
    }

    override fun getGroupTotalCount(id: Long, defaultValue: Long): Long = sp.getLong(getGroupKey(id,KEY_TOTAL_USED),defaultValue)

    override fun setGroupTotalCount(id: Long, value: Long) {
        edit {
            putLong(getGroupKey(id,KEY_TOTAL_USED),value)
        }
    }

    override fun removeGroupTotalUsed(id: Long) {
        edit {
            remove(getAccountKey(id,KEY_TOTAL_USED))
        }
    }

    private fun getAccountKey(id: Long, suffix: String): String = "$PREFIX_ACCOUNT:$id:$suffix"

    private fun getGroupKey(id: Long, suffix: String): String = "$PREFIX_GROUP:$id:$suffix"

    private fun edit(action: SharedPreferences.Editor.() -> Unit) {
        sp.edit(true,action)
    }
}

internal fun SharedPreferences.Editor.putLocalDateTime(key: String, value: LocalDateTime) {
    putString(key,value.toString())
}

internal fun SharedPreferences.getLocalDateTime(key: String, defaultValue: LocalDateTime): LocalDateTime {
    return getString(key,null)?.let { dateTimeText ->
        return LocalDateTime.parse(dateTimeText)
    } ?: defaultValue
}