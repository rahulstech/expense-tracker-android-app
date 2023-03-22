package dreammaker.android.expensetracker.database.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import dreammaker.android.expensetracker.database.Converters;
import dreammaker.android.expensetracker.database.type.Date;
import dreammaker.android.expensetracker.database.type.TransactionType;

import static androidx.room.ColumnInfo.TEXT;
import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "transaction_histories",
foreignKeys = {
        @ForeignKey(entity = Person.class, parentColumns = "id", childColumns = "payeePersonId", onDelete = CASCADE),
        @ForeignKey(entity = Person.class, parentColumns = "id", childColumns = "payerPersonId", onDelete = CASCADE),
        @ForeignKey(entity = Account.class, parentColumns = "id", childColumns = "payeeAccountId", onDelete = CASCADE),
        @ForeignKey(entity = Account.class, parentColumns = "id", childColumns = "payerAccountId", onDelete = CASCADE),
},
indices = {
        @Index(name = "transaction_history_payeePersonId_index", value = "payeePersonId"),
        @Index(name = "transaction_history_payerPersonId_index", value = "payerPersonId"),
        @Index(name = "transaction_history_payeeAccountId_index", value = "payeeAccountId"),
        @Index(name = "transaction_history_payerAccountId_index", value = "payerAccountId")
}
)
public class TransactionHistory implements Cloneable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long id;

    private Long payeePersonId;

    private Long payerPersonId;

    private Long payerAccountId;

    private Long payeeAccountId;

    @ColumnInfo(typeAffinity = TEXT, defaultValue = "0")
    @NonNull
    @TypeConverters(Converters.class)
    private BigDecimal amount;

    @ColumnInfo(typeAffinity = TEXT)
    @NonNull
    @TypeConverters(Converters.class)
    private TransactionType type;

    @ColumnInfo(typeAffinity = TEXT)
    @NonNull
    @TypeConverters(Converters.class)
    private Date date;

    private String description;

    @Ignore
    public TransactionHistory(long id, Long payeePersonId, Long payerPersonId, Long payerAccountId, Long payeeAccountId, BigDecimal amount, TransactionType type, Date date, String description) {
        this.id = id;
        this.payeePersonId = payeePersonId;
        this.payerPersonId = payerPersonId;
        this.payerAccountId = payerAccountId;
        this.payeeAccountId = payeeAccountId;
        setAmount(amount);
        this.type = type;
        this.date = date;
        this.description = description;
    }

    public TransactionHistory() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getPayeePersonId() {
        return payeePersonId;
    }

    public void setPayeePersonId(Long payeePersonId) {
        this.payeePersonId = payeePersonId;
    }

    public Long getPayerPersonId() {
        return payerPersonId;
    }

    public void setPayerPersonId(Long payerPersonId) {
        this.payerPersonId = payerPersonId;
    }

    public Long getPayerAccountId() {
        return payerAccountId;
    }

    public void setPayerAccountId(Long payerAccountId) {
        this.payerAccountId = payerAccountId;
    }

    public Long getPayeeAccountId() {
        return payeeAccountId;
    }

    public void setPayeeAccountId(Long payeeAccountId) {
        this.payeeAccountId = payeeAccountId;
    }

    @NonNull
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(@NonNull BigDecimal amount) {
        Objects.requireNonNull(amount,"amount == null");
        this.amount = amount.setScale(2, RoundingMode.HALF_DOWN);
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull Date date) {
        Objects.requireNonNull(date,"null == date");
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionHistory)) return false;
        TransactionHistory that = (TransactionHistory) o;
        return id == that.id && Objects.equals(payeePersonId, that.payeePersonId) && Objects.equals(payerPersonId, that.payerPersonId) && Objects.equals(payerAccountId, that.payerAccountId) && Objects.equals(payeeAccountId, that.payeeAccountId) && amount.equals(that.amount) && type == that.type && date.equals(that.date) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, payeePersonId, payerPersonId, payerAccountId, payeeAccountId, amount, type, date, description);
    }

    @Override
    public String toString() {
        return "TransactionHistory{" +
                "id=" + id +
                ", payeePersonId=" + payeePersonId +
                ", payerPersonId=" + payerPersonId +
                ", payerAccountId=" + payerAccountId +
                ", payeeAccountId=" + payeeAccountId +
                ", amount=" + amount +
                ", type=" + type +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }

    @NonNull
    @Override
    public TransactionHistory clone() {
        return new TransactionHistory(id,payeePersonId,payerPersonId,payerAccountId,payeeAccountId,new BigDecimal(amount.toString()),type,date.clone(),description);
    }
}
