package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.model.AccountIdName
import java.time.LocalDateTime

data class Account(
    val name: String,
    val balance: Number = 0f,
    val id: Long = 0,
    val lastUsed: LocalDateTime? = null,
    val totalUsed: Long = 0,
) {
    fun toAccountEntity(): AccountEntity = AccountEntity(id,name,balance.toFloat(),lastUsed,totalUsed)
}

fun AccountEntity.toAccount(): Account = Account(name,balance,id,lastUsed,totalUsed ?: 0)

fun AccountIdName.toAccount(): Account = Account(name,0f,id)