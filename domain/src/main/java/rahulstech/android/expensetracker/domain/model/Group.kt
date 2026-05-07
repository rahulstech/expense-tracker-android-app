package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.GroupIdName
import dreammaker.android.expensetracker.database.model.GroupListModel
import java.time.LocalDateTime

/**
 * NOTE: since version domain module version 1.1.0, group balance is considered as
 * total_credit - total_debit
 * so if the balance is position then it is over credit
 * if the balance is negative then it is over debit
 * this change is not migrated in db layer, and will be done later.
 * therefore balance from GroupEntity or any model from database module is reversed here with - (minus)
 */
// TODO: change the group balance calculation after db migration
data class Group(
    val name: String,
    val balance: Double = 0.0,
    val id: Long = 0,
    val lastUsed: LocalDateTime? = null,
    val totalUsed: Long = 0,
) {
    internal fun toGroupEntity(): GroupEntity = GroupEntity(id,name,balance,lastUsed,totalUsed)

    fun isUsedAfterCreate(): Boolean = totalUsed > 1
}

fun GroupEntity.toGroup(): Group = Group(name,-balance,id,lastUsed,totalUsed ?: 0)

fun GroupIdName.toGroup(): Group = Group(name, 0.0, id)

fun GroupListModel.toGroup(): Group = Group(name = name, balance = balance, id = id)
