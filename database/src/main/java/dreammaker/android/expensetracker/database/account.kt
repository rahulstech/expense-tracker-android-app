package dreammaker.android.expensetracker.database

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.gson.annotations.SerializedName
import java.util.Objects

@Entity(tableName = "accounts")
open class Account (
    @SerializedName("_id")
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var accountId: Long,
    @SerializedName("account_name")
    @ColumnInfo(name = "account_name")
    var accountName: String,
    open var balance: Float
): Cloneable {

    @Ignore
    constructor() : this(0, "", 0f)

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other is Account) {
            return other.accountId == accountId && other.accountName == accountName && other.balance == balance;
        }
        return false
    }

    override fun hashCode(): Int = Objects.hash(accountId,accountName,balance)

    @Deprecated("", ReplaceWith("equals(o)"))
    fun equalContents(o: Account?): Boolean = equals(o)

    override fun toString(): String {
        return "Account{" +
                "accountName='" + accountName + '\'' +
                ", accountId=" + accountId +
                ", balance=" + balance +
                '}'
    }

    public override fun clone(): Account = Account(accountId, accountName, balance)
}

data class AccountModel(
    @ColumnInfo(name = "_id")
    val id: Long?,
    @ColumnInfo(name = "account_name")
    val name: String?,
    val balance: Float?
) {
    override fun toString(): String {
        return "AccountModel{id=$id,name='$name',balance=$balance}"
    }
}

@Dao
interface AccountDao {

    @Query("SELECT `_id`, `account_name`, `balance` FROM `accounts`")
    fun getAllAccounts(): LiveData<List<AccountModel>>
}
