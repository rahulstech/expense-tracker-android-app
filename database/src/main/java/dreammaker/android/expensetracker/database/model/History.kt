package dreammaker.android.expensetracker.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.math.BigDecimal
import java.time.LocalDate


@Entity(
    tableName = "histories",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["primaryAccountId"],
            deferred = true,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["secondaryAccountId"],
            deferred = true,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            deferred = true,
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("primaryAccountId", name = "index_histories_primaryAccountId"),
        Index("secondaryAccountId", name = "index_histories_secondaryAccountId"),
        Index("groupId", name = "index_histories_groupId"),
        Index("type", name = "index_histories_type")
    ]
)
data class HistoryEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: String,
    val primaryAccountId: Long?,
    val secondaryAccountId: Long?,
    val groupId: Long?,
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    val amount: BigDecimal,
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    val date: LocalDate,
    val note: String?,
)

data class HistoryDetails(
    @Embedded
    val history: HistoryEntity,

    @Relation(
        entity = AccountEntity::class,
        entityColumn = "id",
        parentColumn = "primaryAccountId",
        projection = ["id","name"]
    )
    val primaryAccount: AccountIdName?,

    @Relation(
        entity = AccountEntity::class,
        entityColumn = "id",
        parentColumn = "secondaryAccountId",
        projection = ["id","name"]
    )
    val secondaryAccount: AccountIdName?,

    @Relation(
        entity = GroupEntity::class,
        entityColumn = "id",
        parentColumn = "groupId",
        projection = ["id","name"]
    )
    val group: GroupIdName?
)

@Deprecated(message = "use TypeTotal")
data class TotalCreditDebit(
    val totalCredit: Double,
    val totalDebit: Double,
)

data class TypeTotal(
    val type: String,
    val total: BigDecimal
)