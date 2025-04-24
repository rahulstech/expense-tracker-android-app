package dreammaker.android.expensetracker.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.DatabaseView
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.TypeConverters
import java.util.Objects

private val TYPES_NEEDS_SOURCE_ACCOUNT = sequenceOf(HistoryType.DEBIT,HistoryType.TRANSFER)

private val TYPES_REQUIRES_DESTINATION_ACCOUNT = sequenceOf(HistoryType.CREDIT,HistoryType.TRANSFER)

enum class HistoryType {
    CREDIT,
    DEBIT,
    TRANSFER,
    ;

    fun needsSourceAccount() = this in TYPES_NEEDS_SOURCE_ACCOUNT

    fun needsDestinationAccount() = this in TYPES_REQUIRES_DESTINATION_ACCOUNT
}

@DatabaseView(viewName = "histories",
    value = " SELECT `_id` AS `id`," +
            " CASE WHEN `type` = 0 THEN 'DEBIT'" +
            " ELSE 'CREDIT' END AS `type`," +
            " CASE `type` WHEN 0 THEN `account_id` ELSE NULL END AS `srcAccountId`," +
            " CASE `type` WHEN 1 THEN `account_id` ELSE NULL END AS `destAccountId`," +
            " `person_id` AS `groupId`,"+
            " `amount`, `date`, `description` AS `note`" +
            " FROM `transactions` WHERE `deleted` = 0" +
            " UNION " +
            " SELECT `id`, 'TRANSFER' AS `type`," +
            " `payer_account_id` AS `srcAccountId`, `payee_account_id` As `destAccountId`," +
            " NULL AS `groupId`, `amount`, `when` AS `date`, `description` AS `note` " +
            " FROM `money_transfers`")

data class History(
    val id: Long,
    val type: HistoryType,
    val srcAccountId: Long?,
    val destAccountId: Long?,
    val groupId: Long?,
    val amount: Float,
    val date: Date,
    val note: String?,
) {
    fun toTransaction(): dreammaker.android.expensetracker.database.Transaction {
        val accountId = if (type.needsSourceAccount()) srcAccountId!! else destAccountId!!
        val personId = groupId
        val type = if (type == HistoryType.DEBIT) dreammaker.android.expensetracker.database.Transaction.TYPE_CREDIT else dreammaker.android.expensetracker.database.Transaction.TYPE_DEBIT
        return Transaction(
            id, accountId, personId, amount, type, date, false, note
        )
    }

    fun toMoneyTransfer(): MoneyTransfer
    = MoneyTransfer(
        id, date, amount, destAccountId!!, srcAccountId!!, note
    )
}

data class HistoryModel(
    val id: Long?,
    val type: HistoryType?,
    val srcAccountId: Long?,
    val destAccountId: Long?,
    val groupId: Long?,
    @Relation(entity = Account::class, parentColumn = "srcAccountId", entityColumn = "_id", projection = ["_id","account_name"])
    val srcAccount: AccountModel?,
    @Relation(entity = Account::class, parentColumn = "destAccountId", entityColumn = "_id", projection = ["_id","account_name"])
    val destAccount: AccountModel?,
    @Relation(entity = Person::class, parentColumn = "groupId", entityColumn = "_id", projection = ["_id","person_name"])
    val group: GroupModel?,
    val amount: Float?,
    val date: Date?,
    val note: String?
) {
    override fun equals(other: Any?): Boolean {
        if (other is HistoryModel) {
            return other.id == id && other.type == type && other.srcAccount == srcAccount && other.destAccount == destAccount
            && other.group == group && other.amount == amount && other.date == date && other.note == note
        }
        return false
    }

    override fun hashCode(): Int =
        Objects.hash(id,type,srcAccount,destAccount,groupId,amount,date,note)

    fun toHistory(): History {
        return History(
            id ?: 0, type!!, srcAccountId, destAccountId, groupId,amount!!,date!!,note
        )
    }
}

@TypeConverters(Converters::class)
@Dao
abstract class HistoryDao(db: ExpensesDatabase) {

    private val TAG = HistoryDao::class.simpleName

    private val expensesDao = db.dao

    @Query("SELECT * FROM `histories` WHERE :start <= `date` AND `date` <= :end ORDER BY `date` DESC")
    @Transaction
    abstract fun getHistoriesBetweenDates(start: Date, end: Date): LiveData<List<HistoryModel>>

    @Query("SELECT * FROM `histories` WHERE `date` = :date")
    @Transaction
    abstract fun getHistoriesForDate(date: Date): LiveData<List<HistoryModel>>

    fun insertHistory(history: History): Long {
        if (history.type == HistoryType.TRANSFER) {
            val moneyTransfer = history.toMoneyTransfer()
            return expensesDao.insertMoneyTransfer(moneyTransfer)
        }
        else {
            val transaction = history.toTransaction()
            return expensesDao.insertTransaction(transaction)
        }
    }

    fun updateHistory(history: History) {
        if (history.type == HistoryType.TRANSFER) {
            val moneyTransfer = history.toMoneyTransfer()
            expensesDao.updateMoneyTransfer(moneyTransfer)
        }
        else {
            val transfer = history.toTransaction()
            expensesDao.updateTransaction(transfer)
        }
    }

    @Query("SELECT * FROM `histories` WHERE `id` = :id AND `type` = :type")
    @Transaction
    abstract fun findHistoryByIdAndType(id: Long, type: HistoryType): LiveData<HistoryModel?>

    @Delete
    fun deleteHistory(history: History) {
        val type = history.type
        if (type == HistoryType.TRANSFER) {
            val moneyTransfer = history.toMoneyTransfer()
            expensesDao.deleteMoneyTransfer(moneyTransfer)
        }
        else {
            val transaction = history.toTransaction()
            expensesDao.deleteTransactions(transaction)
        }
    }
}

