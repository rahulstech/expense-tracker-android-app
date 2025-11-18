package dreammaker.android.expensetracker.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dreammaker.android.expensetracker.database.model.GroupEntity

@Dao
interface GroupDao {

    @Insert
    fun insertGroup(group: GroupEntity): Long

    @Insert
    @Transaction
    fun insertGroups(groups: List<GroupEntity>)

    @Query("SELECT * FROM `groups` ORDER BY `name` ASC")
    fun getLiveAllGroups(): LiveData<List<GroupEntity>>

    @Query("SELECT * FROM `groups`")
    fun getAllGroups(): List<GroupEntity>

    @Query("SELECT * FROM `groups` WHERE `id` = :id")
    fun findGroupById(id: Long): GroupEntity?

    @Query("SELECT * FROM `groups` WHERE `id` = :id")
    fun getLiveGroupById(id: Long): LiveData<GroupEntity?>;

    @Query("SELECT * FROM `groups` WHERE `lastUsed` IS NOT NULL ORDER BY `lastUsed` DESC LIMIT :count")
    fun getLiveRecentlyUsedGroups(count: Int = 3): LiveData<List<GroupEntity>>

    @Update
    fun updateGroup(group: GroupEntity): Int

    @Query("DELETE FROM `groups` WHERE `id` = :id")
    fun deleteGroup(id: Long): Int

    @Query("DELETE FROM `groups` WHERE `id` IN (:ids)")
    fun deleteMultipleGroups(ids: List<Long>): Int
}