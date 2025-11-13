package dreammaker.android.expensetracker.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
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

    @Query("SELECT `_id`, `name`, `due`  FROM `groups` WHERE `_id` = :id")
    fun findGroupById(id: Long): GroupEntity?

    @Query("SELECT `_id` , `name`, `due` FROM `groups` ORDER BY `name` ASC")
    fun getLiveGroups(): LiveData<List<GroupEntity>>

//    @Query("SELECT `_id`, `person_name`, `due` FROM `groups` WHERE `_id` IN " +
//            "(SELECT `groupId` FROM `histories` WHERE `groupId` IS NOT NULL GROUP BY `groupId` ORDER BY Max(`date`) DESC LIMIT 3)")
//    fun getLatestUsedThreeGroups(): LiveData<List<GroupEntity>>

    @Query("SELECT * FROM `groups`")
    fun getGroups(): List<GroupEntity>

    @Update
    fun updateGroup(group: GroupEntity)

    @Delete
    fun deleteGroup(group: GroupEntity)

    @Query("DELETE FROM `groups` WHERE `_id` IN(:ids)")
    fun deleteMultipleGroups(ids: List<Long>)
}