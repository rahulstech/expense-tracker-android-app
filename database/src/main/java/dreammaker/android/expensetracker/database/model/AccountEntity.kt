package dreammaker.android.expensetracker.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val name: String,

    val balance: Float
)