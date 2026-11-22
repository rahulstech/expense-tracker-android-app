package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.GroupEntity
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * NOTE: since version domain module version 1.1.0, group balance is considered as
 * total_credit - total_debit
 * so if the balance is position then it is over credit
 * if the balance is negative then it is over debit
 * this change is not migrated in db layer, and will be done later.
 * therefore balance from GroupEntity or any model from database module is reversed here with - (minus)
 */
data class Group(
    val name: String,
    val balance: BigDecimal = BigDecimal.ZERO,
    val id: Long = 0,
    val lastUsed: LocalDateTime? = null,
    val totalUsed: Long = 0,
) {

    companion object {
        val UNKNOWN = Group(id = -1L, name = "Unknown")

        val DELETED = Group(id = -2L, name = "Deleted")
    }
    internal fun toGroupEntity(): GroupEntity = GroupEntity(id,name,balance,lastUsed,totalUsed)

    fun isUsedAfterCreate(): Boolean = totalUsed > 1
}
