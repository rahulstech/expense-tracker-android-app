package rahulstech.android.expensetracker.domain.impl

import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.GroupDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.LocalCache
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.toGroup
import java.time.LocalDateTime
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    db: IExpenseDatabase,
    private val cache: LocalCache
): GroupRepository {

    private val groupDao: GroupDao = db.groupDao

    // --- New Coroutine and Flow based methods ---

    override suspend fun createGroup(group: Group): Group {
        val _group = group.copy(lastUsed = LocalDateTime.now(), totalUsed = 1)
        val id = groupDao.insert(_group.toGroupEntity())
        cache.setGroupTotalCount(id, 1)
        return _group.copy(id = id)
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

    override suspend fun editGroup(group: Group): Boolean {
        val cachedTotalUsed = cache.getGroupTotalCount(group.id)
        val _group = group.copy(lastUsed = LocalDateTime.now(), totalUsed = cachedTotalUsed + 1)
        groupDao.update(_group.toGroupEntity())
        cache.setGroupTotalCount(group.id, _group.totalUsed)
        return true
    }

    override suspend fun creditGroupDue(id: Long, amount: Number) {
        val group = getGroup(id)
        group?.let {
            val updatedGroup = it.copy(balance = it.balance + amount.toDouble())
            editGroup(updatedGroup)
        }
    }

    override suspend fun debitGroupDue(id: Long, amount: Number) {
        val group = getGroup(id)
        group?.let {
            val updatedGroup = it.copy(balance = it.balance - amount.toDouble())
            editGroup(updatedGroup)
        }
    }

    override suspend fun removeGroup(id: Long) {
        groupDao.delete(id)
        cache.removeGroupTotalUsed(id)
    }

    override suspend fun removeMultipleGroups(ids: List<Long>) {
        groupDao.deleteMultiple(ids)
        ids.forEach { cache.removeGroupTotalUsed(it) }
    }
}
