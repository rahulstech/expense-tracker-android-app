package rahulstech.android.expensetracker.domain

import kotlinx.coroutines.flow.Flow
import rahulstech.android.expensetracker.domain.model.Group

interface GroupRepository {

    // --- New Coroutine and Flow based methods ---

    suspend fun createGroup(group: Group): Group

    suspend fun getGroup(id: Long): Group?

    fun getGroupById(id: Long): Flow<Group?>

    fun getAllGroups(): Flow<List<Group>>

    fun getRecentlyUsedGroups(count: Int = 3): Flow<List<Group>>

    suspend fun editGroup(group: Group): Boolean

    suspend fun creditGroupDue(id: Long, amount: Number)

    suspend fun debitGroupDue(id: Long, amount: Number)

    suspend fun removeGroup(id: Long)

    suspend fun removeMultipleGroups(ids: List<Long>)
}