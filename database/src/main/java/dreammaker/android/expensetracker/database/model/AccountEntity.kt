package dreammaker.android.expensetracker.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "accounts")
data class AccountEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val name: String,

    val balance: Float,

    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    val lastUsed: LocalDateTime? = null,

    val totalUsed: Long? = null,
)

data class AccountIdName(
    val id: Long,
    val name: String
)