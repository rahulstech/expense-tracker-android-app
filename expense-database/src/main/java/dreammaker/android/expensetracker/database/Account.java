package dreammaker.android.expensetracker.database;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import dreammaker.android.expensetracker.util.Check;

@Entity(tableName = "accounts")
public class Account {

    @ColumnInfo(name = "account_name")
    @NonNull
    @SerializedName("account_name")
    private String accountName;

    @ColumnInfo(name = "_id")
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName("_id")
    private long accountId;

    @ColumnInfo(defaultValue = "0")
    @NonNull
    private float balance;

    public Account(long accountId, String accountName, float balance) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.balance = balance;
    }

    @Ignore
    public Account() {}

    public long getAccountId() { return accountId; }

    public void setAccountId(long accountId) { this.accountId = accountId; }

    @NonNull
    public String getAccountName() { return accountName; }

    public void setAccountName(@NonNull String accountName) { this.accountName = accountName; }

    public float getBalance() { return balance; }

    public void setBalance(float balance) { this.balance = balance; }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o != null && o instanceof Account){
            return this.accountId == ((Account) o).accountId;
        }
        return false;
    }

    public boolean equalContents(@Nullable Account o) {
        if (null != o) {
            return o.getAccountId() == this.getAccountId()
                    && Check.isEqualString(o.getAccountName(),this.getAccountName())
                    && 0 == Float.compare(this.balance,o.balance);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountName='" + accountName + '\'' +
                ", accountId=" + accountId +
                ", balance=" + balance +
                '}';
    }

    @NonNull
    @Override
    public Account clone() {
        return new Account(accountId,accountName,balance);
    }
}
