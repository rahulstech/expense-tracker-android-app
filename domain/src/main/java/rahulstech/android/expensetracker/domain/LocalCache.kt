package rahulstech.android.expensetracker.domain

interface LocalCache{

    fun getAccountTotalUsed(id: Long,defaultValue: Long = 0): Long

    fun setAccountTotalUsed(id: Long,value: Long)

    fun removeAccountTotalUsed(id: Long)

    fun getGroupTotalCount(id: Long, defaultValue: Long = 0): Long

    fun setGroupTotalCount(id: Long, value: Long = 0)

    fun removeGroupTotalUsed(id: Long)
}