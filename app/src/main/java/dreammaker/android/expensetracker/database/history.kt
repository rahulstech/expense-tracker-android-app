package dreammaker.android.expensetracker.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.DatabaseView
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.TypeConverters
import dreammaker.android.expensetracker.util.Date
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
interface HistoryDao {

    @Query("SELECT * FROM `histories` WHERE `date` BETWEEN :start AND :end ORDER BY `date` DESC")
    @Transaction
    fun getHistoriesBetweenDates(start: Date, end: Date): LiveData<List<HistoryModel>>
}

