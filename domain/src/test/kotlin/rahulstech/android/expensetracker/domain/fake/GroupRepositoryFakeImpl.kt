package rahulstech.android.expensetracker.domain.fake

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group

class GroupRepositoryFakeImpl: GroupRepository {

    private val _groups = mutableMapOf(
        1L to Group("Group 1", 1000f, 1L),
        2L to Group("Group 2", 2000f, 2L),
    )

    val groups: Map<Long, Group> = _groups

    override fun insertGroup(group: Group): Group = group

    override fun findGroupById(id: Long): Group? = _groups[id]

    override fun getLiveGroupById(id: Long): LiveData<Group?> = MutableLiveData(_groups[id])

    override fun getLiveAllGroups(): LiveData<List<Group>> = MutableLiveData(_groups.values.toList())

    override fun getLiveRecentlyUsedGroups(count: Int): LiveData<List<Group>> = MutableLiveData(emptyList())

    override fun updateGroup(group: Group): Boolean {
        return _groups[group.id]?.let { oldGroup ->
            _groups[group.id] = group
            true
        } ?: false
    }

    override fun creditDue(id: Long, amount: Number) {
        _groups[id]?.let { oldGroup ->
            _groups[id] = oldGroup.copy(balance = oldGroup.balance + amount.toFloat())
        }
    }

    override fun debitDue(id: Long, amount: Number) {
        _groups[id]?.let { oldGroup ->
            _groups[id] = oldGroup.copy(balance = oldGroup.balance - amount.toFloat())
        }
    }

    override fun deleteGroup(id: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteMultipleGroups(ids: List<Long>) {
        TODO("Not yet implemented")
    }
}