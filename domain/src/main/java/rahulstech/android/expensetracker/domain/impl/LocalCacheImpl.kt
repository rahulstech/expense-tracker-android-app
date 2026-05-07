package rahulstech.android.expensetracker.domain.impl


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import rahulstech.android.expensetracker.domain.LocalCache
import javax.inject.Inject

class LocalCacheImpl @Inject constructor(
    @ApplicationContext context: Context
): LocalCache {
    companion object {
        private const val SP_NAME = "rahulstech.android.expensetracker.domain.LocalCache"
        private const val PREFIX_ACCOUNT = "account"
        private const val KEY_TOTAL_USED = "total_used"
        private const val KEY_DEFAULT_ACCOUNT = "default_account"
    }

    private val sp: SharedPreferences = context.applicationContext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE)

    override fun removeAccountTotalUsed(id: Long) {
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

    private fun edit(action: SharedPreferences.Editor.() -> Unit) {
        sp.edit(true,action)
    }
}