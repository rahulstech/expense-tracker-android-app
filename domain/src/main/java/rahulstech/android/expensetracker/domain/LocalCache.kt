package rahulstech.android.expensetracker.domain

import kotlinx.coroutines.flow.Flow

interface LocalCache{

    fun getAccountTotalUsed(id: Long,defaultValue: Long = 0): Long

    fun setAccountTotalUsed(id: Long,value: Long)

    fun removeAccountTotalUsed(id: Long)

    fun getGroupTotalCount(id: Long, defaultValue: Long = 0): Long

    fun setGroupTotalCount(id: Long, value: Long = 0)

    fun removeGroupTotalUsed(id: Long)

    fun setDefaultAccountId(id: Long)

    fun getDefaultAccountId(): Long?

    fun getDefaultAccountIdFlow(): Flow<Long?>

    fun removeDefaultAccount()
}