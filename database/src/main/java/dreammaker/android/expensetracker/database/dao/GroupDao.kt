package dreammaker.android.expensetracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.GroupListModel
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Insert
    suspend fun insert(group: GroupEntity): Long

    @Deprecated("Use insertMultiple instead", ReplaceWith("insertMultiple(groups)"))
    @Insert
    @Transaction
    fun insertGroups(groups: List<GroupEntity>)

    @Insert
    @Transaction
    suspend fun insertMultiple(groups: List<GroupEntity>)

    @Query("SELECT id, name, balance AS due FROM `groups` ORDER BY `name` ASC")
    fun getAllGroupsFlow(): Flow<List<GroupListModel>>

    @Query("SELECT * FROM `groups`")
    suspend fun getAllGroups(): List<GroupEntity>

    @Deprecated("Use findByIdFlow instead")
    @Query("SELECT * FROM `groups` WHERE `id` = :id")
    fun findGroupById(id: Long): GroupEntity?

    @Query("SELECT * FROM `groups` WHERE `id` = :id")
    fun findByIdFlow(id: Long): Flow<GroupEntity?>

    @Query("SELECT id, name, due FROM (SELECT id, name, balance AS due FROM `groups` WHERE `lastUsed` IS NOT NULL ORDER BY `lastUsed` DESC LIMIT :count) ORDER BY `name` ASC")
    fun getRecentlyUsedGroupsFlow(count: Int = 3): Flow<List<GroupListModel>>

    @Deprecated("Use update instead", ReplaceWith("update(group)"))
    @Update
    fun updateGroup(group: GroupEntity): Int

    @Update
    suspend fun update(group: GroupEntity): Int

    @Query("DELETE FROM `groups` WHERE `id` = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM `groups` WHERE `id` IN (:ids)")
    suspend fun deleteMultiple(ids: List<Long>): Int
}
