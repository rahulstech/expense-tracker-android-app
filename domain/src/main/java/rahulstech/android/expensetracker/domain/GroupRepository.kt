package rahulstech.android.expensetracker.domain

import androidx.lifecycle.LiveData
import rahulstech.android.expensetracker.domain.model.Group

interface GroupRepository {

    fun insertGroup(group: Group): Group

    fun findGroupById(id: Long): Group?

    fun getLiveGroupById(id: Long): LiveData<Group?>

    fun getLiveAllGroups(): LiveData<List<Group>>

    fun getLiveRecentlyUsedGroups(count: Int = 3): LiveData<List<Group>>

    fun updateGroup(group: Group): Boolean

    fun creditDue(id: Long, amount: Number)

    fun debitDue(id: Long, amount: Number)

    fun deleteGroup(id: Long)

    fun deleteMultipleGroups(ids: List<Long>)
}