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

enum class HistoryType {
    CREDIT,
    DEBIT,
    TRANSFER,
    ;

    fun needsSourceAccount() = true

    fun needsDestinationAccount() = this == TRANSFER

    fun needsGroup() = this == CREDIT || this == DEBIT
}

@DatabaseView(viewName = "histories",
    value = " SELECT `_id` AS `id`," +
            " CASE WHEN `type` = 0 THEN 'DEBIT'" +
            " ELSE 'CREDIT' END AS `type`," +
            " `account_id` AS `primaryAccountId`," +
            " NULL AS `secondaryAccountId`," +
            " `person_id` AS `groupId`,"+
            " `amount`, `date`, `description` AS `note`" +
            " FROM `transactions` WHERE `deleted` = 0" +
            " UNION " +
            " SELECT `id`, 'TRANSFER' AS `type`," +
            " `payer_account_id` AS `primaryAccountId`, `payee_account_id` As `secondaryAccountId`," +
            " NULL AS `groupId`, `amount`, `when` AS `date`, `description` AS `note` " +
            " FROM `money_transfers`")

data class History(
    val id: Long,
    val type: HistoryType,
    val primaryAccountId: Long?,
    val secondaryAccountId: Long?,
    val groupId: Long?,
    val amount: Float,
    val date: Date,
    val note: String?,
) {
    fun toTransaction(): dreammaker.android.expensetracker.database.Transaction {
        val accountId = primaryAccountId!!
        val personId = groupId
        val type = if (type == HistoryType.DEBIT) 0 else 1
        return Transaction(
            id, accountId, personId, amount, type, date, false, note
        )
    }

    fun toMoneyTransfer(): MoneyTransfer
    = MoneyTransfer(
        id, date, amount, secondaryAccountId!!, primaryAccountId!!, note
    )
}

data class HistoryModel(
    val id: Long?,
    val type: HistoryType?,
    val primaryAccountId: Long?,
    val secondaryAccountId: Long?,
    val groupId: Long?,
    @Relation(entity = Account::class, parentColumn = "primaryAccountId", entityColumn = "_id", projection = ["_id","account_name"])
    val primaryAccount: AccountModel?,
    @Relation(entity = Account::class, parentColumn = "secondaryAccountId", entityColumn = "_id", projection = ["_id","account_name"])
    val secondaryAccount: AccountModel?,
    @Relation(entity = Person::class, parentColumn = "groupId", entityColumn = "_id", projection = ["_id","person_name"])
    val group: GroupModel?,
    val amount: Float?,
    val date: Date?,
    val note: String?
) {
    override fun equals(other: Any?): Boolean {
        if (other is HistoryModel) {
            return other.id == id && other.type == type && other.primaryAccountId == primaryAccountId && other.secondaryAccountId == secondaryAccountId
            && other.group == group && other.amount == amount && other.date == date && other.note == note
        }
        return false
    }

    override fun hashCode(): Int =
        Objects.hash(id,type,primaryAccountId,secondaryAccountId,groupId,amount,date,note)

    fun toHistory(): History {
        return History(
            id ?: 0, type!!, primaryAccountId, secondaryAccountId, groupId,amount!!,date!!,note
        )
    }
}

@TypeConverters(Converters::class)
@Dao
abstract class HistoryDao(db: ExpensesDatabase) {

    private val TAG = HistoryDao::class.simpleName

    private val expensesDao = db.dao
    private val expenseBackupDao = db.backupDao

    @Query("SELECT * FROM `histories` WHERE :start <= `date` AND `date` <= :end ORDER BY `date` ASC")
    @Transaction
    abstract fun getHistoriesBetweenDates(start: Date, end: Date): LiveData<List<HistoryModel>>

    @Query("SELECT * FROM `histories` WHERE :start <= `date` AND `date` <= :end AND ( `primaryAccountId` = :accountId or `secondaryAccountId` = :accountId) ORDER BY `date` ASC")
    @Transaction
    abstract fun getHistoriesBetweenDatesOnlyForAccount(start: Date, end: Date, accountId: Long): LiveData<List<HistoryModel>>

    @Query("SELECT * FROM `histories` WHERE :start <= `date` AND `date` <= :end AND `groupId` = :groupId ORDER BY `date` ASC")
    @Transaction
    abstract fun getHistoriesBetweenDatesOnlyForGroup(start: Date, end: Date, groupId: Long): LiveData<List<HistoryModel>>

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

    @Transaction
    open fun insertHistories(histories: List<History>) {
        val transactions = histories.filter { history -> history.type != HistoryType.TRANSFER }.map { history -> history.toTransaction() }
        val moneyTransfers = histories.filter { history -> history.type == HistoryType.TRANSFER }.map { history -> history.toMoneyTransfer() }

        expenseBackupDao.insertTransactions(transactions)
        expenseBackupDao.insertMoneyTransfers(moneyTransfers)
    }

    @Query("SELECT * FROM `histories` LIMIT :size OFFSET :from")
    abstract fun getAllHistories(from: Long, size: Long): List<History>
}

