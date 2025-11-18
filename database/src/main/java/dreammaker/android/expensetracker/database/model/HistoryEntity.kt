package dreammaker.android.expensetracker.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dreammaker.android.expensetracker.database.Converters
import java.time.LocalDate

enum class HistoryType {
    CREDIT,
    DEBIT,
    TRANSFER,
    ;

    fun needsSourceAccount() = true

    fun needsDestinationAccount() = this == TRANSFER

    fun needsGroup() = this == CREDIT || this == DEBIT
}

@Entity(
    tableName = "histories",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["primaryAccountId"],
            deferred = true,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["secondaryAccountId"],
            deferred = true,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            deferred = true,
            onDelete = ForeignKey.SET_NULL
        ),
    ],
    indices = [
        Index("primaryAccountId", name = "index_histories_primaryAccountId"),
        Index("secondaryAccountId", name = "index_histories_secondaryAccountId"),
        Index("groupId", name = "index_histories_groupId")
    ]
)
data class HistoryEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @TypeConverters(Converters::class)
    val type: HistoryType,
    val primaryAccountId: Long?,
    val secondaryAccountId: Long?,
    val groupId: Long?,
    val amount: Float,
    @TypeConverters(Converters::class)
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    val date: LocalDate,
    val note: String?,
)