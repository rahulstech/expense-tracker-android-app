package dreammaker.android.expensetracker.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.DatabaseView
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.TypeConverters
import java.util.Objects

enum class HistoryType {
    CREDIT,
    DEBIT,
    EXPENSE,
    INCOME,
    TRANSFER,
}

@DatabaseView(viewName = "histories",
    value = "SELECT `_id` AS `id`, CASE `type` WHEN 0 THEN 'DEBIT' ELSE 'CREDIT' END AS `type`," +
            " CASE `type` WHEN 0 THEN `account_id` ELSE NULL END AS `srcAccountId`," +
            " CASE `type` WHEN 1 THEN `account_id` ELSE NULL END AS `destAccountId`," +
            " CASE `type` WHEN 0 THEN `person_id` ELSE NULL END AS `destPersonId`," +
            " CASE `type` WHEN 1 THEN `person_id` ELSE NULL END AS `srcPersonId`," +
            " `amount`, `date`, `description` AS `note`" +
            " FROM `transactions` WHERE `deleted` = 0 AND `person_id` IS NOT NULL" +
            " UNION " +
            " SELECT `id`, 'TRANSFER' AS `type`," +
            " `payer_account_id` AS `srcAccountId`, `payee_account_id` As `destAccountId`," +
            " NULL AS `srcPersonId`, NULL AS `destPersonId`, `amount`, `when` AS `date`, `description` AS `note` " +
            " FROM `money_transfers` ")

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
)

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
        if (other == this) return true
        if (other is HistoryModel) {
            return other.id == id && other.type == type && other.srcAccount == srcAccount && other.destAccount == destAccount
            && other.srcPerson == srcPerson && other.destPerson == destPerson && other.amount == amount && other.date == date && other.note == note
        }
        return false
    }

    override fun hashCode(): Int =
        Objects.hash(id,type,srcAccount,destAccount,srcPerson,destPerson,amount,date,note)
}

@TypeConverters(Converters::class)
@Dao
abstract class HistoryDao(private val db: ExpensesDatabase) {

    private val expensesDao = db.dao

    @Query("SELECT * FROM `histories` WHERE :start <= `date` AND `date` <= :end ORDER BY `date` DESC")
    @Transaction
    abstract fun getHistoriesBetweenDates(start: Date, end: Date): LiveData<List<HistoryModel>>

    @Query("SELECT * FROM `histories` WHERE `date` = :date")
    @Transaction
    abstract fun getHistoriesForDate(date: Date): LiveData<List<HistoryModel>>

    fun insertHistory(history: History): Long {
        if (history.type == HistoryType.TRANSFER) {
            val moneyTransfer = MoneyTransfer(
                0,
                history.date,
                history.amount,
                history.destAccountId!!,
                history.srcAccountId!!,
                history.note
            )
            return expensesDao.insertMoneyTransfer(moneyTransfer)
        }
        else {
            val accountId = if (history.type == HistoryType.DEBIT)  history.destAccountId else  history.srcAccountId
            val personId = if (history.type == HistoryType.DEBIT)  history.srcPersonId else  history.destPersonId
            val transfer = Transaction(
                0,
                accountId!!,
                personId,
                history.amount,
                if (history.type == HistoryType.DEBIT) 0 else 1,
                history.date,
                false,
                history.note
            )
            return expensesDao.insertTransaction(transfer)
        }
    }

    @Query("SELECT * FROM `histories` WHERE `id` = :id AND `type` = :type")
    abstract fun findHistoryByIdAndType(id: Long, type: HistoryType): LiveData<HistoryModel?>
}

