package dreammaker.android.expensetracker.database.entity;

import java.time.LocalDate;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;

@Entity(tableName = "transaction_histories")
@SuppressWarnings("unused")
public class TransactionHistory {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private Long payeeAccountId;

    private Long payerAccountId;

    private Long payeePersonId;

    private Long payerPersonId;

    @NonNull
    private TransactionType type;

    @NonNull
    private Currency amount;

    @NonNull
    private LocalDate when;

    private String description;

    public TransactionHistory() {
        this.amount = Currency.ZERO;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getPayeeAccountId() {
        return payeeAccountId;
    }

    public void setPayeeAccountId(Long payeeAccountId) {
        this.payeeAccountId = payeeAccountId;
    }

    public Long getPayerAccountId() {
        return payerAccountId;
    }

    public void setPayerAccountId(Long payerAccountId) {
        this.payerAccountId = payerAccountId;
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

    @NonNull
    public TransactionType getType() {
        return type;
    }

    public void setType(@NonNull TransactionType type) {
        this.type = type;
    }

    @NonNull
    public Currency getAmount() {
        return amount;
    }

    public void setAmount(@NonNull Currency amount) {
        this.amount = amount;
    }

    @NonNull
    public LocalDate getWhen() {
        return when;
    }

    public void setWhen(@NonNull LocalDate when) {
        this.when = when;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    @Override
    public String toString() {
        return "TransactionHistory{" +
                "id=" + id +
                ", payeeAccountId=" + payeeAccountId +
                ", payerAccountId=" + payerAccountId +
                ", payeePersonId=" + payeePersonId +
                ", payerPersonId=" + payerPersonId +
                ", type=" + type +
                ", amount=" + amount +
                ", when=" + when +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionHistory)) return false;
        TransactionHistory history = (TransactionHistory) o;
        return id == history.id && Objects.equals(payeeAccountId, history.payeeAccountId) && Objects.equals(payerAccountId, history.payerAccountId) && Objects.equals(payeePersonId, history.payeePersonId) && Objects.equals(payerPersonId, history.payerPersonId) && type == history.type && amount.equals(history.amount) && when.equals(history.when) && Objects.equals(description, history.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, payeeAccountId, payerAccountId, payeePersonId, payerPersonId, type, amount, when, description);
    }

    @NonNull
    public TransactionHistory copy() {
        TransactionHistory copy = new TransactionHistory();
        copy.id = id;
        copy.payeeAccountId = payeeAccountId;
        copy.payerAccountId = payerAccountId;
        copy.payeePersonId = payeePersonId;
        copy.payerPersonId = payerPersonId;
        copy.type = type;
        copy.amount = amount.copy();
        copy.when = LocalDate.from(when);
        copy.description = description;
        return copy;
    }
}
