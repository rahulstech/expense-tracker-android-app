package dreammaker.android.expensetracker.database.model

import androidx.room.Embedded
import androidx.room.Relation

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
