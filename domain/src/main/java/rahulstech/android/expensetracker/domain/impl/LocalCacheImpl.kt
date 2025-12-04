package rahulstech.android.expensetracker.domain.impl


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import rahulstech.android.expensetracker.domain.LocalCache
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import java.time.LocalDateTime

internal class LocalCacheImpl(
    context: Context
): LocalCache {
    companion object {
        private const val SP_NAME = "rahulstech.android.expensetracker.domain.LocalCache"
        private const val PREFIX_ACCOUNT = "account"
        private const val PREFIX_GROUP = "group"
        private const val KEY_TOTAL_USED = "total_used"
        private const val KEY_DEFAULT_ACCOUNT = "default_account"
        private const val KEY_DEFAULT_GROUP = "default_group"
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

    override fun setDefaultAccountId(id: Long) {
        edit {
            putLong(KEY_DEFAULT_ACCOUNT,id)
        }
        _defaultAccountIdFlow.value = id
    }

    override fun getDefaultAccountId(): Long? =
        sp.getLong(KEY_DEFAULT_ACCOUNT, 0L).takeIf { id -> id > 0 }

    private val _defaultAccountIdFlow by lazy { MutableStateFlow(getDefaultAccountId()) }

    override fun getDefaultAccountIdFlow(): Flow<Long?> = _defaultAccountIdFlow

    override fun removeDefaultAccount() {
        edit {
            remove(KEY_DEFAULT_ACCOUNT)
        }
        _defaultAccountIdFlow.value = null
    }

    private fun getAccountKey(id: Long, suffix: String): String = buildString {
        append(PREFIX_ACCOUNT)
        append(":")
        append(id)
        append(":")
        append(suffix)
    }

    private fun getGroupKey(id: Long, suffix: String): String = buildString {
            append(PREFIX_GROUP)
            append(":")
            append(id)
            append(":")
            append(suffix)
        }

    private fun edit(action: SharedPreferences.Editor.() -> Unit) {
        sp.edit(true,action)
    }
}

internal fun generateKey(vararg parts: Any): String = buildString {
    append(parts[0])
    parts.forEach { part ->
        append(":")
        append(part)
    }
}

internal fun SharedPreferences.Editor.putLocalDateTime(key: String, value: LocalDateTime) {
    putString(key,value.toString())
}

internal fun SharedPreferences.getLocalDateTime(key: String, defaultValue: LocalDateTime): LocalDateTime {
    return getString(key,null)?.let { dateTimeText ->
        LocalDateTime.parse(dateTimeText)
    } ?: defaultValue
}

internal fun SharedPreferences.Editor.putAccount(key: String, account: Account) {
    val obj = JSONObject().apply {
        put("id", account.id)
        put("name", account.name)
        put("balance", account.balance)
    }
    val json = obj.toString()
    putString(key,json)
}

internal fun SharedPreferences.getAccount(key: String): Account? = getString(key,null)?.let { json ->
        val obj = JSONObject(json)
        val id = obj.getLong("id")
        val name = obj.getString("name")
        val balance = obj.getDouble("balance").toFloat()
        Account(name,balance,id)
    }

internal fun SharedPreferences.Editor.putGroup(key: String, group: Group) {
    val obj = JSONObject().apply {
        put("id", group.id)
        put("name", group.name)
        put("due", group.balance)
    }
    val json = obj.toString()
    putString(key,json)
}

internal fun SharedPreferences.getGroup(key: String): Group? = getString(key,null)?.let { json ->
    val obj = JSONObject(json)
    val id = obj.getLong("id")
    val name = obj.getString("name")
    val due = obj.getDouble("due").toFloat()
    Group(name,due,id)
}