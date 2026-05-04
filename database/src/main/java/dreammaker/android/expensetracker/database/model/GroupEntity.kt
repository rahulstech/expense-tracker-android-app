package dreammaker.android.expensetracker.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "groups")
data class GroupEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val name: String,

    val balance: Double,

    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    val lastUsed: LocalDateTime? = null,

    val totalUsed: Long? = null,
)

data class GroupIdName(
    val id: Long,
    val name: String
)

data class GroupListModel(
    val id: Long,
    val name: String,
    val due: Double
)
