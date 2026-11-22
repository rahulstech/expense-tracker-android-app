package rahulstech.android.expensetracker.domain.model

import dreammaker.android.expensetracker.database.model.AccountEntity
import dreammaker.android.expensetracker.database.model.AccountIdName
import dreammaker.android.expensetracker.database.model.AccountListModel
import dreammaker.android.expensetracker.database.model.GroupEntity
import dreammaker.android.expensetracker.database.model.GroupIdName
import dreammaker.android.expensetracker.database.model.GroupListModel
import dreammaker.android.expensetracker.database.model.HistoryDetails
import dreammaker.android.expensetracker.database.model.HistoryEntity
import java.math.BigDecimal
import kotlin.reflect.KClass

fun AccountEntity.toAccount(isDefault: Boolean = false): Account =
    Account(name,balance,id,lastUsed,totalUsed ?: 0, isDefault)

fun AccountIdName.toAccount(): Account = Account(name = name,id = id)

fun AccountListModel.toAccount(): Account = Account(name = name, balance = balance, id = id)


fun GroupEntity.toGroup(): Group = Group(name,-balance,id,lastUsed,totalUsed ?: 0)

fun GroupIdName.toGroup(): Group = Group(name, BigDecimal.ZERO, id)

fun GroupListModel.toGroup(): Group = Group(name,balance,id)

fun HistoryEntity.toHistory(): History  {
    val historyType = HistoryType.valueOf(type)
    return when(historyType) {
        HistoryType.CREDIT -> {
            History.CreditHistory(
                id = id,
                date = date,
                amount = amount,
                note = note,
                primaryAccountId = primaryAccountId!!,
                groupId = groupId,
            )
        }
        HistoryType.DEBIT -> {
            History.DebitHistory(
                id = id,
                date = date,
                amount = amount,
                note = note,
                primaryAccountId = primaryAccountId!!,
                groupId = groupId,
            )
        }
        HistoryType.CREDIT_INCOME -> {
            History.CreditIncomeHistory(
                id = id,
                date = date,
                amount = amount,
                note = note,
                primaryAccountId = primaryAccountId!!,
            )
        }
        HistoryType.DEBIT_EXPENSE -> {
            History.DebitExpenseHistory(
                id = id,
                date = date,
                amount = amount,
                note = note,
                primaryAccountId = primaryAccountId!!,
            )
        }
        HistoryType.TRANSFER -> {
            History.TransferHistory(
                id = id,
                date = date,
                amount = amount,
                note = note,
                primaryAccountId = primaryAccountId!!,
                secondaryAccountId = secondaryAccountId!!
            )
        }
    }
}

fun HistoryDetails.toHistory(): History {
    val historyType = HistoryType.valueOf(history.type)
    return when(historyType) {
        HistoryType.CREDIT -> {
            History.CreditHistory(
                id = history.id,
                date = history.date,
                amount = history.amount,
                note = history.note,
                primaryAccountId = history.primaryAccountId!!,
                groupId = history.groupId,
                primaryAccount = primaryAccount?.toAccount() ?: Account.DELECTED,
                group = group?.toGroup() ?: Group.DELETED
            )
        }
        HistoryType.DEBIT -> {
            History.DebitHistory(
                id = history.id,
                date = history.date,
                amount = history.amount,
                note = history.note,
                primaryAccountId = history.primaryAccountId!!,
                groupId = history.groupId,
                primaryAccount = primaryAccount?.toAccount() ?: Account.DELECTED,
                group = group?.toGroup() ?: Group.DELETED
            )
        }
        HistoryType.CREDIT_INCOME -> {
            History.CreditIncomeHistory(
                id = history.id,
                date = history.date,
                amount = history.amount,
                note = history.note,
                primaryAccountId = history.primaryAccountId!!,
                primaryAccount = primaryAccount?.toAccount() ?: Account.DELECTED,
            )
        }
        HistoryType.DEBIT_EXPENSE -> {
            History.DebitExpenseHistory(
                id = history.id,
                date = history.date,
                amount = history.amount,
                note = history.note,
                primaryAccountId = history.primaryAccountId!!,
                primaryAccount = primaryAccount?.toAccount() ?: Account.DELECTED,
            )
        }
        HistoryType.TRANSFER -> {
            History.TransferHistory(
                id = history.id,
                date = history.date,
                amount = history.amount,
                note = history.note,
                primaryAccountId = history.primaryAccountId!!,
                secondaryAccountId = history.secondaryAccountId!!,
                primaryAccount = primaryAccount?.toAccount() ?: Account.DELECTED,
                secondaryAccount = secondaryAccount?.toAccount() ?: Account.DELECTED
            )
        }
    }
}

internal fun List<KClass<History>>.toHistoryTypes(): List<HistoryType> =
    this.map {
        when(it) {
            History.CreditHistory::class -> {
                HistoryType.CREDIT
            }
            History.DebitHistory::class -> {
                HistoryType.DEBIT
            }
            History.CreditIncomeHistory::class -> {
                HistoryType.CREDIT_INCOME
            }
            History.DebitExpenseHistory::class -> {
                HistoryType.DEBIT_EXPENSE
            }
            History.TransferHistory::class -> {
                HistoryType.TRANSFER
            }
        }
    }