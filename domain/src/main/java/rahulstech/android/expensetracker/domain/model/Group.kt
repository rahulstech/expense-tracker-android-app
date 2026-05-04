package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.GroupIdName
import dreammaker.android.expensetracker.database.model.GroupListModel
import java.time.LocalDateTime

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

fun GroupEntity.toGroup(): Group = Group(name,balance,id,lastUsed,totalUsed ?: 0)

fun GroupIdName.toGroup(): Group = Group(name, 0.0, id)

fun GroupListModel.toGroup(): Group = Group(name = name, balance = due, id = id)
