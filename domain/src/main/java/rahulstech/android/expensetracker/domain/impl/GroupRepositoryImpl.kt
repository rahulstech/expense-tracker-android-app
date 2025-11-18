package rahulstech.android.expensetracker.domain.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dreammaker.android.expensetracker.database.IExpenseDatabase
import dreammaker.android.expensetracker.database.dao.GroupDao
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.LocalCache
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.toGroup
import java.time.LocalDateTime

internal class GroupRepositoryImpl(
    db: IExpenseDatabase,
    private val cache: LocalCache
): GroupRepository {

    private val groupDao: GroupDao = db.groupDao

    override fun insertGroup(group: Group): Group {
        val _group = group.copy(lastUsed = LocalDateTime.now(), totalUsed = 1)
        val id = groupDao.insertGroup(_group.toGroupEntity())
        cache.setGroupTotalCount(id,1)
        return _group.copy(id=id)
    }

    override fun findGroupById(id: Long): Group? =
        groupDao.findGroupById(id)?.toGroup()

    override fun getLiveGroupById(id: Long): LiveData<Group?> =
        groupDao.getLiveGroupById(id).map { it?.toGroup() }

    override fun getLiveAllGroups(): LiveData<List<Group>> =
        groupDao.getLiveAllGroups().map { entities -> entities.map { it.toGroup() } }

    override fun getLiveRecentlyUsedGroups(count: Int): LiveData<List<Group>> =
        groupDao.getLiveRecentlyUsedGroups(count).map { entities -> entities.map { it.toGroup() } }

    override fun updateGroup(group: Group): Boolean {
        val cachedTotalUsed = cache.getGroupTotalCount(group.id)
        val _group = group.copy(lastUsed = LocalDateTime.now(), totalUsed = cachedTotalUsed+1)
        val changes = groupDao.updateGroup(_group.toGroupEntity())
        if (changes==1) {
            cache.setGroupTotalCount(group.id,_group.totalUsed)
            return true
        }
        return false
    }

    override fun creditDue(id: Long, amount: Number) {
        val entity = groupDao.findGroupById(id)
        entity?.let {
            val group = it.toGroup()
            val updatedGroup = group.copy(due = group.due.toFloat() + amount.toFloat())
            updateGroup(updatedGroup)
        }
    }

    override fun debitDue(id: Long, amount: Number) {
        val entity = groupDao.findGroupById(id)
        entity?.let {
            val group = it.toGroup()
            val updatedGroup = group.copy(due = group.due.toFloat() - amount.toFloat())
            updateGroup(updatedGroup)
        }
    }

    override fun deleteGroup(id: Long) {
        groupDao.deleteGroup(id)
        cache.removeGroupTotalUsed(id)
    }

    override fun deleteMultipleGroups(ids: List<Long>) {
        groupDao.deleteMultipleGroups(ids)
        ids.forEach { cache.removeGroupTotalUsed(it) }
    }
}