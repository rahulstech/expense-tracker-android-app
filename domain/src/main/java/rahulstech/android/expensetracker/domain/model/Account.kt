package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.AccountEntity
import java.math.BigDecimal
import java.time.LocalDateTime

data class Account(
    val name: String,
    val balance: BigDecimal = BigDecimal.ZERO,
    val id: Long = 0,
    val lastUsed: LocalDateTime? = null,
    val totalUsed: Long = 0,
    val isDefault: Boolean = false,
) {
    companion object {

        val UNKNOWN = Account(id = -1L, name = "Unknown")

        val DELECTED = Account(id = -2L, name = "Deleted Account")
    }

    internal fun toAccountEntity(): AccountEntity = AccountEntity(id,name, balance, lastUsed, totalUsed)

    fun isUsedAfterCreate(): Boolean = totalUsed > 1
}

