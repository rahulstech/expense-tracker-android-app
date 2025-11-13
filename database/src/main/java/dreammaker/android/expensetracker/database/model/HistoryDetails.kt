package dreammaker.android.expensetracker.database.model

import androidx.room.Relation

data class HistoryDetails(
    val history: HistoryEntity,

    @Relation(
        entity = AccountEntity::class,
        entityColumn = "id",
        parentColumn = "primaryAccountId",
        projection = ["id","name"]
    )
    val primaryAccount: AccountEntity?,

    @Relation(
        entity = AccountEntity::class,
        entityColumn = "id",
        parentColumn = "primaryAccountId",
        projection = ["id","name"]
    )
    val secondaryAccount: AccountEntity?,

    @Relation(
        entity = GroupEntity::class,
        entityColumn = "id",
        parentColumn = "groupId",
    )
    val group: GroupEntity?
)
