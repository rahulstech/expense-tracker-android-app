package dreammaker.android.expensetracker.database

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

data class Group(
    @ColumnInfo(name = "_id")
    val id: Long,
    @ColumnInfo(name = "person_name")
    val name: String,
    @ColumnInfo(name = "due")
    val balance: Float
)

data class GroupModel(
    @ColumnInfo(name = "_id")
    val id: Long?,
    @ColumnInfo(name = "person_name")
    val name: String?,
    @ColumnInfo(name = "due")
    val balance: Float?
) {
    fun toGroup(): Person
    = Person(id ?: 0, name!!, balance ?: 0f)
}

@Dao
interface GroupDao {

    @Insert
    fun insertGroup(group: Person): Long

    @Update
    fun updateGroup(group: Person)

    @Delete
    fun deleteGroup(group: Person)

    @Query("DELETE FROM `persons` WHERE `_id` IN(:ids)")
    fun deleteMultipleGroups(ids: List<Long>)

    @Query("SELECT `_id`, `person_name`, `due`  FROM `persons` WHERE `_id` = :id")
    fun findGroupById(id: Long): LiveData<GroupModel?>

    @Query("SELECT `_id` , `person_name`, `due` FROM `persons` ORDER BY `person_name` ASC")
    fun getAllGroups(): LiveData<List<GroupModel>>

    @Query("SELECT `_id`, `person_name`, `due` FROM `persons` WHERE `_id` IN " +
            "(SELECT `groupId` FROM `histories` WHERE `groupId` IS NOT NULL GROUP BY `groupId` ORDER BY Max(`date`) DESC LIMIT 3)")
    fun getLatestUsedThreeGroups(): LiveData<List<GroupModel>>

    @Insert
    @Transaction
    fun insertGroups(groups: List<Person>)

    @Query("SELECT * FROM `persons`")
    fun getGroups(): List<Group>
}