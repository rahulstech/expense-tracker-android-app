package rahulstech.android.expensetracker.domain

import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group

interface LocalCache{

    fun getAccountTotalUsed(id: Long,defaultValue: Long = 0): Long

    fun setAccountTotalUsed(id: Long,value: Long)

    fun removeAccountTotalUsed(id: Long)

    fun getGroupTotalCount(id: Long, defaultValue: Long = 0): Long

    fun setGroupTotalCount(id: Long, value: Long = 0)

    fun removeGroupTotalUsed(id: Long)

    fun setDefaultAccount(account: Account)

    fun getDefaultAccountId(): Long?

    fun removeDefaultAccount()

    fun setDefaultGroup(group: Group)

    fun getDefaultGroupId(): Long?

    fun removeDefaultGroup()
}