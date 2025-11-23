package rahulstech.android.expensetracker.domain

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.domain.model.Group

interface GroupRepository {

    fun insertGroup(group: Group): Group

    fun findGroupById(id: Long): Group?

    fun getLiveGroupById(id: Long): LiveData<Group?>

    fun getLiveAllGroups(): LiveData<List<Group>>

    fun getLiveRecentlyUsedGroups(count: Int = 3): LiveData<List<Group>>

    fun getDefaultGroup(): Flow<Group?>

    fun updateGroup(group: Group): Boolean

    fun creditDue(id: Long, amount: Number)

    fun debitDue(id: Long, amount: Number)

    fun changeDefaultGroup(group: Group?)

    fun deleteGroup(id: Long)

    fun deleteMultipleGroups(ids: List<Long>)
}