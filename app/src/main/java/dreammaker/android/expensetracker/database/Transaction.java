package dreammaker.android.expensetracker.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;

import static dreammaker.android.expensetracker.database.ExpensesContract.Indexes.TRANSACTIONS_ACCOUNT_ID_INDEX;
import static dreammaker.android.expensetracker.database.ExpensesContract.Indexes.TRANSACTIONS_PERSON_ID_INDEX;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.TRANSACTIONS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.ACCOUNT_ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.AMOUNT;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.DATE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.DESCRIPTION;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.PERSON_ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns._ID;

@Entity(tableName = TRANSACTIONS_TABLE,
foreignKeys = {@ForeignKey(
        entity = Account.class,
        parentColumns = {ExpensesContract.AccountsColumns._ID},
        childColumns = {ACCOUNT_ID},
        onDelete = ForeignKey.CASCADE
), @ForeignKey(
        entity = Person.class,
        parentColumns = {ExpensesContract.PersonsColumns._ID},
        childColumns = {PERSON_ID},
        onDelete = ForeignKey.CASCADE
)},
indices = {@Index(name = TRANSACTIONS_ACCOUNT_ID_INDEX, value = {ACCOUNT_ID}),
        @Index(name = TRANSACTIONS_PERSON_ID_INDEX, value = {PERSON_ID})
})
public class Transaction {

    public static final int TYPE_CREDIT = ExpensesContract.TransactionsColumns.TYPE_CREDIT;

    public static final int TYPE_DEBIT = ExpensesContract.TransactionsColumns.TYPE_DEBIT;

    @ColumnInfo(name = _ID, typeAffinity = ColumnInfo.INTEGER)
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long transactionId;

    @ColumnInfo(name = ACCOUNT_ID, typeAffinity = ColumnInfo.INTEGER)
    @NonNull
    private long accountId;

    @ColumnInfo(name = PERSON_ID, typeAffinity = ColumnInfo.INTEGER)
    @Nullable
    private Long personId;

    @ColumnInfo(name = AMOUNT, typeAffinity = ColumnInfo.REAL, defaultValue = "0")
    @NonNull
    private float amount;

    @ColumnInfo(name = TYPE, typeAffinity = ColumnInfo.INTEGER)
    @NonNull
    private int type;

    @ColumnInfo(name = DATE, typeAffinity = ColumnInfo.TEXT)
    @NonNull
    @TypeConverters(Converters.class)
    private Date date;

    @ColumnInfo(name = DESCRIPTION, typeAffinity = ColumnInfo.TEXT)
    private String description;

    public Transaction(long transactionId, long accountId, Long personId, float amount, int type, @NonNull Date date, String description) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.personId = personId;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.description = description;
    }

    @Ignore
    public Transaction(long accountId, @Nullable Long personId, float amount, int type, @NonNull Date date, String description) {
        this.accountId = accountId;
        this.personId = personId;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.description = description;
    }

    @Ignore
    public Transaction() {
        this(0,0,null,0,TYPE_DEBIT,new Date(),null);
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getAccountId() {
        return accountId;
    }

    @Nullable
    public Long getPersonId() {
        return personId;
    }

    public float getAmount() {
        return amount;
    }

    public int getType() {
        return type;
    }

    @NonNull
    public Date getDate() { return date; }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setPersonId(@Nullable Long personId) {
        this.personId = personId;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof Transaction){
            return this.getTransactionId() == ((Transaction) o).getTransactionId();
        }
        return false;
    }

    public boolean equalContents(@Nullable Transaction t) {
        if (null != t) {
            return this.getAccountId() == t.getAccountId()
                    && Check.isEquals(this.getPersonId(),t.getPersonId())
                    && this.getAmount() == t.getAmount()
                    && this.getType() == t.getType()
                    && Check.isEquals(this.date,t.getDate())
                    && Check.isEqualString(this.getDescription(),t.getDescription());
        }
        return false;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", accountId=" + accountId +
                ", personId=" + personId +
                ", amount=" + amount +
                ", type=" + type +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }

    @NonNull
    @Override
    public Transaction clone() {
        return new Transaction(transactionId,accountId,personId,amount,type,date,description);
    }
}
