package dreammaker.android.expensetracker.database;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import dreammaker.android.expensetracker.database.type.Date;

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
@Deprecated
@SuppressWarnings({"unused", "deprecation", "DeprecatedIsStillUsed"})
public class Transaction implements Cloneable {

    @Deprecated
    public static final int TYPE_CREDIT = ExpensesContract.TransactionsColumns.TYPE_CREDIT;

    @Deprecated
    public static final int TYPE_DEBIT = ExpensesContract.TransactionsColumns.TYPE_DEBIT;

    @ColumnInfo(name = _ID)
    @PrimaryKey(autoGenerate = true)
    @SerializedName(_ID)
    private long transactionId;

    @ColumnInfo(name = ACCOUNT_ID)
    @SerializedName(ACCOUNT_ID)
    private long accountId;

    @ColumnInfo(name = PERSON_ID)
    @SerializedName(PERSON_ID)
    private Long personId;

    private boolean deleted;

    @ColumnInfo(name = AMOUNT, typeAffinity = ColumnInfo.REAL)
    @NonNull
    @SerializedName(AMOUNT)
    private BigDecimal totalAmount;

    @TypeConverters(Converters.class)
    @SerializedName(TYPE)
    private int type;

    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    @NonNull
    @TypeConverters(Converters.class)
    @SerializedName(DATE)
    private Date date;

    @SerializedName(DESCRIPTION)
    private String description;

    public Transaction(long transactionId, long accountId, Long personId,
                       @NonNull BigDecimal totalAmount, int type, @NonNull Date date,
                       boolean deleted, String description) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.personId = personId;
        this.deleted = deleted;
        this.totalAmount = totalAmount;
        this.date = date;
        this.description = description;
        setType(type);
    }

    @Ignore
    public Transaction() {
        this(0,0,null,BigDecimal.ZERO,TYPE_CREDIT,new Date(),false,null);
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public Long getPersonId() {
        return personId;
    }

    @Deprecated
    public float getAmount() {
        return null == totalAmount ? 0 : totalAmount.floatValue();
    }

    public int getType() {
        return type;
    }

    @NonNull
    public Date getDate() { return date; }

    public boolean isDeleted() {
        return false;
    }

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

    @Deprecated
    public void setAmount(float amount) {
        setTotalAmount(new BigDecimal(amount).setScale(2, RoundingMode.HALF_DOWN));
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(@NonNull BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return transactionId == that.transactionId && accountId == that.accountId && deleted == that.deleted && type == that.type && Objects.equals(personId, that.personId) && totalAmount.equals(that.totalAmount) && date.equals(that.date) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, accountId, personId, deleted, totalAmount, type, date, description);
    }

    @NonNull
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", accountId=" + accountId +
                ", personId=" + personId +
                ", deleted=" + deleted +
                ", totalAmount=" + totalAmount +
                ", type=" + type +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @NonNull
    @Override
    public Transaction clone() {
        return new Transaction(transactionId,accountId,personId,totalAmount,type,date,deleted,description);
    }
}
