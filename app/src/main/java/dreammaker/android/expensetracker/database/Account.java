package dreammaker.android.expensetracker.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import dreammaker.android.expensetracker.util.Check;

import static dreammaker.android.expensetracker.database.ExpensesContract.AboutAccountColumns.BALANCE;
import static dreammaker.android.expensetracker.database.ExpensesContract.AccountsColumns.ACCOUNT_NAME;
import static dreammaker.android.expensetracker.database.ExpensesContract.AccountsColumns._ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.ACCOUNTS_TABLE;

@Entity(tableName = ACCOUNTS_TABLE)
public class Account {

    @ColumnInfo(name = ACCOUNT_NAME, typeAffinity = ColumnInfo.TEXT)
    @NonNull
    private String accountName;

    @ColumnInfo(name = _ID, typeAffinity = ColumnInfo.INTEGER)
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long accountId;


    @ColumnInfo(name = BALANCE, typeAffinity = ColumnInfo.REAL, defaultValue = "0")
    @NonNull
    private float balance;

    public Account(long accountId, String accountName, float balance) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.balance = balance;
    }

    @Ignore
    @Deprecated
    public Account(long accountId, String accountName) {
        this(accountId,accountName,0);
    }

    @Ignore
    @Deprecated
    public Account(String accountName){
        this(0, accountName,0);
    }

    @Ignore
    public Account() { this(0,"", 0); }

    public long getAccountId() { return accountId; }

    public void setAccountId(long accountId) { this.accountId = accountId; }

    @NonNull
    public String getAccountName() { return accountName; }

    public void setAccountName(@NonNull String accountName) { this.accountName = accountName; }


    public float getBalance() { return balance; }

    public void setBalance(float balance) { this.balance = balance; }


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
