package dreammaker.android.expensetracker.database;

import com.google.gson.annotations.SerializedName;

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

@Entity(tableName = "transactions",
foreignKeys = {@ForeignKey(
        entity = Account.class,
        parentColumns = {"_id"},
        childColumns = {"account_id"},
        onDelete = ForeignKey.CASCADE
), @ForeignKey(
        entity = Person.class,
        parentColumns = {"_id"},
        childColumns = {"person_id"},
        onDelete = ForeignKey.CASCADE
)},
indices = {@Index(name = "transactions_account_id_index", value = {"account_id"}),
        @Index(name = "transactions_person_id_index", value = {"person_id"})
})
public class Transaction implements Cloneable {

    @Deprecated
    public static final int TYPE_CREDIT = 0;

    @Deprecated
    public static final int TYPE_DEBIT = 1;

    @ColumnInfo(name = "_id")
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName("_id")
    private long transactionId;

    @ColumnInfo(name = "account_id")
    @NonNull
    @SerializedName("account_id")
    private long accountId;

    @ColumnInfo(name = "person_id")
    @NonNull
    @SerializedName("person_id")
    private long personId;

    @ColumnInfo(defaultValue = "0")
    @NonNull
    private float amount;

    @NonNull
    @TypeConverters(Converters.class)
    private Date date;

    @ColumnInfo(defaultValue = "0")
    @NonNull
    private boolean deleted;

    private String description;

    public Transaction(long transactionId, long accountId, long personId, float amount, @NonNull Date date, boolean deleted, String description) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.personId = personId;
        this.amount = amount;
        this.date = date;
        this.deleted = deleted;
        this.description = description;
    }

    @Deprecated
    @Ignore
    public Transaction(long transactionId, long accountId, Long personId, float amount, int type, @NonNull Date date, boolean deleted, String description) {
        this(transactionId,accountId,personId,amount,date,deleted,description);
        setType(type);
    }

    @Ignore
    @Deprecated
    public Transaction(long accountId, @Nullable Long personId, float amount, int type, @NonNull Date date, String description) {
        this(0,accountId,personId,amount,type,date,false, description);
    }

    @Ignore
    public Transaction() {
        this(0,0,0,
                0,Date.today(),false,null);
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

    public float getAmount() {
        return amount;
    }

    @Deprecated
    public int getType() {
        return amount < 0 ? TYPE_CREDIT : TYPE_DEBIT;
    }

    public Date getDate() { return date; }

    public boolean isDeleted() {
        return deleted;
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

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Deprecated
    public void setType(int type) {
        amount = TYPE_CREDIT == type ? -1*Math.abs(amount) : Math.abs(amount);
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

    @Override
    public boolean equals(@Nullable Object o) {
        if (null != o && o instanceof Transaction){
            return this.getTransactionId() == ((Transaction) o).getTransactionId();
        }
        return false;
    }

    public boolean equalContents(@Nullable Transaction t) {
        if (null != t) {
            return this.getAccountId() == t.getAccountId()
                    && Check.isEquals(this.getPersonId(),t.getPersonId())
                    && 0 == Float.compare(amount,t.amount)
                    && this.getType() == t.getType()
                    && Check.isEquals(this.date,t.getDate())
                    && this.deleted == t.deleted
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
                ", date=" + date +
                ", deleted=" + deleted +
                ", description='" + description + '\'' +
                '}';
    }

    @NonNull
    @Override
    public Transaction clone() {
        return new Transaction(transactionId,accountId,personId,
                amount,date.clone(), deleted,description);
    }
}
