package rahulstech.android.expensetracker.domain

import kotlinx.coroutines.flow.Flow

interface LocalCache{

    fun removeAccountTotalUsed(id: Long)

    fun setDefaultAccountId(id: Long)

    fun getDefaultAccountId(): Long?

    fun getDefaultAccountIdFlow(): Flow<Long?>

    fun removeDefaultAccount()
}