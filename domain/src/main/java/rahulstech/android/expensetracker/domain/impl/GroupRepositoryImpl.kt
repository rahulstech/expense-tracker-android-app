package rahulstech.android.expensetracker.domain.impl

import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.GroupDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.toGroup
import java.time.LocalDateTime
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    db: IExpenseDatabase
): GroupRepository {

    private val groupDao: GroupDao = db.groupDao

    // --- New Coroutine and Flow based methods ---

    override suspend fun createGroup(group: Group): Group {
        val newGroup = group.copy(lastUsed = LocalDateTime.now(), totalUsed = 1)
        val id = groupDao.insert(newGroup.toGroupEntity())
        return newGroup.copy(id = id)
    }

    override suspend fun getGroup(id: Long): Group? {
        return groupDao.findByIdFlow(id).first()?.toGroup()
    }

    override fun getGroupById(id: Long): Flow<Group?> {
        return groupDao.findByIdFlow(id).map { it?.toGroup() }.flowOn(Dispatchers.IO)
    }

    override fun getAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroupsFlow().map { entities ->
            entities.map { it.toGroup() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getRecentlyUsedGroups(count: Int): Flow<List<Group>> {
        return groupDao.getRecentlyUsedGroupsFlow(count).map { entities ->
            entities.map { it.toGroup() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getThreeFrequentlyUsedGroups(): Flow<List<Group>> {
        return groupDao.getFrequentlyUsedGroupsFlow(3).map { entities ->
            entities.map { it.toGroup() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun editGroup(group: Group): Boolean {
        val updatedGroup = group.copy(lastUsed = LocalDateTime.now())
        groupDao.update(updatedGroup.toGroupEntity())
        return true
    }

    override suspend fun creditGroupDue(id: Long, amount: Double) {
        val entity = groupDao.findByIdFlow(id).first()
        entity?.let {
            val updatedGroup = it.copy(
                balance = it.balance + amount,
                lastUsed = LocalDateTime.now(),
                totalUsed = it.totalUsed?.let { totalUsed -> totalUsed+1 } ?: 1
            )
            groupDao.update(updatedGroup)
        }
    }

    override suspend fun debitGroupDue(id: Long, amount: Double) {
        val entity = groupDao.findByIdFlow(id).first()
        entity?.let {
            val updatedGroup = it.copy(
                balance = it.balance - amount,
                lastUsed = LocalDateTime.now(),
                totalUsed = it.totalUsed?.let { totalUsed -> totalUsed+1 } ?: 1
            )
            groupDao.update(updatedGroup)
        }
    }

    override suspend fun removeGroup(id: Long) {
        groupDao.delete(id)
    }

    override suspend fun removeMultipleGroups(ids: List<Long>) {
        groupDao.deleteMultiple(ids)
    }
}
