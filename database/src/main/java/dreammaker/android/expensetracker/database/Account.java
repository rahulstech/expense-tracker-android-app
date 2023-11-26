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
import dreammaker.android.expensetracker.database.type.Currency;

import static dreammaker.android.expensetracker.database.ExpensesContract.AboutAccountColumns.BALANCE;
import static dreammaker.android.expensetracker.database.ExpensesContract.AccountsColumns.ACCOUNT_NAME;
import static dreammaker.android.expensetracker.database.ExpensesContract.AccountsColumns._ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.ACCOUNTS_TABLE;

@Entity(tableName = ACCOUNTS_TABLE)
@Deprecated
public class Account {

    @NonNull
    private String accountName;

    @PrimaryKey(autoGenerate = true)
    private long accountId;

    @NonNull
    private Currency accountBalance;

    @Ignore
    @Deprecated
    public Account(long accountId, String accountName, float balance) {
        this(accountId,accountName,BigDecimal.valueOf(balance));
    }

    @Deprecated
    @Ignore
    public Account(long accountId, String accountName, @NonNull BigDecimal totalBalance) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.accountBalance = Currency.valueOf(totalBalance);
    }

    public Account() {
        this.accountBalance = Currency.ZERO;
    }

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
        return accountBalance.getValue();
    }

    @Deprecated
    public void setTotalBalance(BigDecimal totalBalance) {
        Objects.requireNonNull(totalBalance,"totalBalance == null");
        setAccountBalance(Currency.valueOf(totalBalance));
    }

    @NonNull
    public Currency getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(@NonNull Currency accountBalance) {
        this.accountBalance = accountBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return accountId == account.accountId && accountName.equals(account.accountName) && accountBalance.equals(account.accountBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountName, accountId, accountBalance);
    }

    @Deprecated
    public boolean equalContents(@Nullable Account o) {
        return equals(o);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountName='" + accountName + '\'' +
                ", accountId=" + accountId +
                ", balance=" + accountBalance +
                '}';
    }

    @NonNull
    @Override
    public Account clone() {
        Account copy = new Account();
        copy.setAccountId(accountId);
        copy.setAccountName(accountName);
        copy.setAccountBalance(accountBalance);
        return copy;
    }
}
