package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.GroupEntity

data class Group(
    val name: String,
    val due: Float,
    val id: Long = 0,
)

fun GroupEntity.toGroup(): Group = Group(name,due,id)