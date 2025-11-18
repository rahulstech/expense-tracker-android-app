package dreammaker.android.expensetracker.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dreammaker.android.expensetracker.database.Converters
import java.time.LocalDateTime

@Entity(tableName = "groups")
data class GroupEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val name: String,

    val due: Float,

    @TypeConverters(Converters::class)
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    val lastUsed: LocalDateTime? = null,

    val totalUsed: Long? = null,
)

data class GroupIdName(
    val id: Long,
    val name: String
)
