package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.GroupIdName
import java.time.LocalDateTime

data class Group(
    val name: String,
    val balance: Float = 0f,
    val id: Long = 0,
    val lastUsed: LocalDateTime? = null,
    val totalUsed: Long = 0,
    val isDefault: Boolean = false,
) {
    fun toGroupEntity(): GroupEntity = GroupEntity(id,name,balance,lastUsed,totalUsed)

    fun isUsedAfterCreate(): Boolean = totalUsed > 1
}

fun GroupEntity.toGroup(): Group = Group(name,balance,id,lastUsed,totalUsed ?: 0)

fun GroupIdName.toGroup(): Group = Group(name,0f,id)