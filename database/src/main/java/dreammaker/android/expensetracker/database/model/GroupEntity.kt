package dreammaker.android.expensetracker.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "groups")
data class GroupEntity (
    @ColumnInfo(name = "_id")
    val id: Long,

    val name: String,

    val due: Float
)
