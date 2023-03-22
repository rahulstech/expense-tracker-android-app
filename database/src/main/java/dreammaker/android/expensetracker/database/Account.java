package dreammaker.android.expensetracker.database;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import static dreammaker.android.expensetracker.database.ExpensesContract.AboutAccountColumns.BALANCE;
import static dreammaker.android.expensetracker.database.ExpensesContract.AccountsColumns.ACCOUNT_NAME;
import static dreammaker.android.expensetracker.database.ExpensesContract.AccountsColumns._ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.ACCOUNTS_TABLE;

@Entity(tableName = ACCOUNTS_TABLE)
@Deprecated
public class Account {

    @ColumnInfo(name = ACCOUNT_NAME, typeAffinity = ColumnInfo.TEXT)
    @NonNull
    @SerializedName(ACCOUNT_NAME)
    private String accountName;

    @ColumnInfo(name = _ID, typeAffinity = ColumnInfo.INTEGER)
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName(_ID)
    private long accountId;

    @ColumnInfo(name = BALANCE, typeAffinity = ColumnInfo.REAL, defaultValue = "0")
    @NonNull
    @TypeConverters(Converters.class)
    @SerializedName(BALANCE)
    private BigDecimal totalBalance;

    @Ignore
    @Deprecated
    public Account(long accountId, String accountName, float balance) {
        this(accountId,accountName,BigDecimal.valueOf(balance));
    }

    public Account(long accountId, String accountName, @NonNull BigDecimal totalBalance) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.totalBalance = totalBalance;
    }

    @Ignore
    public Account() { this(0,null, BigDecimal.ZERO); }

    public long getAccountId() { return accountId; }

    public void setAccountId(long accountId) { this.accountId = accountId; }

    @NonNull
    public String getAccountName() { return accountName; }

    public void setAccountName(@NonNull String accountName) { this.accountName = accountName; }


    @Deprecated
    public float getBalance() {
        return getTotalBalance().floatValue();
    }

    @Deprecated
    public void setBalance(float balance) {
        setTotalBalance(BigDecimal.valueOf(balance));
    }

    @NonNull
    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        Objects.requireNonNull(totalBalance,"totalBalance == null");
        this.totalBalance = totalBalance;
    }


    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof Account){
            return this.accountId == ((Account) o).accountId;
        }
        return false;
    }

    public boolean equalContents(@Nullable Account o) {
        if (null != o) {
            return o.getAccountId() == this.getAccountId()
                    && Objects.deepEquals(accountName,o.accountName)
                    && 0 == totalBalance.compareTo(o.totalBalance);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountName='" + accountName + '\'' +
                ", accountId=" + accountId +
                ", balance=" + totalBalance.toPlainString() +
                '}';
    }

    @NonNull
    @Override
    public Account clone() {
        return new Account(accountId,accountName,totalBalance);
    }
}
