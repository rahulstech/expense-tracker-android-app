package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.AccountEntity

data class Account(
    val id: Long,
    val name: String,
    val balance: Float,
) {
    fun toAccountEntity(): AccountEntity = AccountEntity(id,name,balance)
}

fun AccountEntity.toAccount(): Account = Account(id,name,balance)