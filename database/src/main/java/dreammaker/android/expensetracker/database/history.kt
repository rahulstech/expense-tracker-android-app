package dreammaker.android.expensetracker.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.DatabaseView
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.TypeConverters
import java.util.Objects

private val TYPES_NEEDS_SOURCE_ACCOUNT = sequenceOf(HistoryType.DEBIT,HistoryType.EXPENSE,HistoryType.TRANSFER)

private val TYPES_REQUIRES_DESTINATION_ACCOUNT = sequenceOf(HistoryType.CREDIT,HistoryType.INCOME,HistoryType.TRANSFER)

enum class HistoryType {
    CREDIT,
    DEBIT,
    EXPENSE,
    INCOME,
    TRANSFER,
    ;

    fun needsSourceAccount() = this in TYPES_NEEDS_SOURCE_ACCOUNT

    fun needsDestinationAccount() = this in TYPES_REQUIRES_DESTINATION_ACCOUNT

    fun needsSourcePerson() = this == CREDIT

    fun needsDestinationPerson() = this == DEBIT
}

@DatabaseView(viewName = "histories",
    value = " SELECT `_id` AS `id`," +
            " CASE WHEN `type` = 0 AND `person_id` IS NOT NULL THEN 'DEBIT'" +
            " WHEN `type` = 0 AND `person_id` IS NULL THEN 'EXPENSE'" +
            " WHEN `type` = 1 AND `person_id` IS NULL THEN 'INCOME'" +
            " ELSE 'CREDIT' END AS `type`," +
            " CASE `type` WHEN 0 THEN `account_id` ELSE NULL END AS `srcAccountId`," +
            " CASE `type` WHEN 1 THEN `account_id` ELSE NULL END AS `destAccountId`," +
            " CASE `type` WHEN 0 THEN `person_id` ELSE NULL END AS `destPersonId`," +
            " CASE `type` WHEN 1 THEN `person_id` ELSE NULL END AS `srcPersonId`," +
            " `amount`, `date`, `description` AS `note`" +
            " FROM `transactions` WHERE `deleted` = 0" +
            " UNION " +
            " SELECT `id`, 'TRANSFER' AS `type`," +
            " `payer_account_id` AS `srcAccountId`, `payee_account_id` As `destAccountId`," +
            " NULL AS `srcPersonId`, NULL AS `destPersonId`, `amount`, `when` AS `date`, `description` AS `note` " +
            " FROM `money_transfers`")

data class History(
    val id: Long,
    val type: HistoryType,
    val srcAccountId: Long?,
    val destAccountId: Long?,
    val srcPersonId: Long?,
    val destPersonId: Long?,
    val amount: Float,
    val date: Date,
    val note: String?,
) {
    fun toTransaction(): dreammaker.android.expensetracker.database.Transaction {
        val accountId = if (type == HistoryType.DEBIT || type == HistoryType.EXPENSE)  srcAccountId else  destAccountId
        val personId = if (type == HistoryType.DEBIT)  destPersonId else  srcPersonId
        val type = if (type == HistoryType.DEBIT || type == HistoryType.EXPENSE) dreammaker.android.expensetracker.database.Transaction.TYPE_CREDIT else dreammaker.android.expensetracker.database.Transaction.TYPE_DEBIT
        return Transaction(
            id, accountId!!, personId, amount, type, date, false, note
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
    val srcPersonId: Long?,
    val destPersonId: Long?,
    @Relation(entity = Account::class, parentColumn = "srcAccountId", entityColumn = "_id", projection = ["_id","account_name"])
    val srcAccount: AccountModel?,
    @Relation(entity = Account::class, parentColumn = "destAccountId", entityColumn = "_id", projection = ["_id","account_name"])
    val destAccount: AccountModel?,
    @Relation(entity = Person::class, parentColumn = "srcPersonId", entityColumn = "_id", projection = ["_id","person_name"])
    val srcPerson: PersonModel?,
    @Relation(entity = Person::class, parentColumn = "destPersonId", entityColumn = "_id", projection = ["_id","person_name"])
    val destPerson: PersonModel?,
    val amount: Float?,
    val date: Date?,
    val note: String?
) {
    override fun equals(other: Any?): Boolean {
        if (other is HistoryModel) {
            return other.id == id && other.type == type && other.srcAccount == srcAccount && other.destAccount == destAccount
            && other.srcPerson == srcPerson && other.destPerson == destPerson && other.amount == amount && other.date == date && other.note == note
        }
        return false
    }

    override fun hashCode(): Int =
        Objects.hash(id,type,srcAccount,destAccount,srcPerson,destPerson,amount,date,note)

    fun toHistory(): History {
        return History(
            id ?: 0, type!!, srcAccountId, destAccountId, srcPersonId, destPersonId,amount!!,date!!,note
        )
    }
}

@TypeConverters(Converters::class)
@Dao
abstract class HistoryDao(private val db: ExpensesDatabase) {

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
            Log.i(TAG," insertHistory: history=$history transaction=$transaction")
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

