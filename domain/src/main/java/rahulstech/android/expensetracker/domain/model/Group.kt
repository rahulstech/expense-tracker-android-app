package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.GroupIdName
import java.time.LocalDateTime

data class Group(
    val name: String,
    val due: Number = 0f,
    val id: Long = 0,
    val lastUsed: LocalDateTime? = null,
    val totalUsed: Long = 0
) {
    fun toGroupEntity(): GroupEntity = GroupEntity(id,name,due.toFloat(),lastUsed,totalUsed)
}

fun GroupEntity.toGroup(): Group = Group(name,due,id,lastUsed,totalUsed ?: 0)

fun GroupIdName.toGroup(): Group = Group(name,0f,id)