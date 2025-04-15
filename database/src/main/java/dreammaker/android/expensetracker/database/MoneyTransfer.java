package dreammaker.android.expensetracker.database;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Objects;

@Entity(
        tableName = "money_transfers",
        foreignKeys = {
                @ForeignKey(entity = Account.class, parentColumns = {ExpensesContract.AccountsColumns._ID},
                        childColumns = {"payee_account_id"}, onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Account.class, parentColumns = {ExpensesContract.AccountsColumns._ID},
                        childColumns = {"payer_account_id"}, onDelete =  ForeignKey.CASCADE)
        },
        indices = {
                @Index(name = "money_transfer_payee_account_id_index",value = {"payee_account_id"}),
                @Index(name = "money_transfer_payer_account_id_index", value = {"payer_account_id"})
        }
)
public class MoneyTransfer implements Cloneable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo( typeAffinity = ColumnInfo.INTEGER)
    private long id;
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    @NonNull
    @TypeConverters(Converters.class)
    private Date when;
    @ColumnInfo(typeAffinity = ColumnInfo.REAL, defaultValue = "0")
    private float amount;
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    @NonNull
    private long payee_account_id;
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    @NonNull
    private long payer_account_id;
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    private String description;

    public MoneyTransfer(long _id, @NotNull Date when, float amount, long payee_account_id, long payer_account_id, String description) {
        this.id = _id;
        this.when = when;
        this.amount = amount;
        this.payee_account_id = payee_account_id;
        this.payer_account_id = payer_account_id;
        this.description = description;
    }

    public MoneyTransfer() {
        this(0,new Date(),0,0,0,null);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public Date getWhen() {
        return when;
    }

    public void setWhen(@NotNull Date when) {
        this.when = when;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public long getPayee_account_id() {
        return payee_account_id;
    }

    public void setPayee_account_id(long payee_account_id) {
        this.payee_account_id = payee_account_id;
    }

    public long getPayer_account_id() {
        return payer_account_id;
    }

    public void setPayer_account_id(long payer_account_id) {
        this.payer_account_id = payer_account_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "InputMoneyTransfer{" +
                "id=" + id +
                ", when=" + when +
                ", amount=" + amount +
                ", payee_account_id=" + payee_account_id +
                ", payer_account_id=" + payer_account_id +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoneyTransfer that = (MoneyTransfer) o;
        return id == that.id;
    }

    public boolean equalsContent(MoneyTransfer that) {
        if (null != that) {
            return id == that.id &&
                    Float.compare(that.amount, amount) == 0 &&
                    payee_account_id == that.payee_account_id &&
                    payer_account_id == that.payer_account_id &&
                    when.equals(that.when) &&
                    Objects.equals(description, that.description);
        }
        return false;
    }

    @NonNull
    @Override
    public MoneyTransfer clone() {
        return new MoneyTransfer(this.id,this.when.clone(),
                this.amount,this.payee_account_id,this.payer_account_id,this.description);
    }
}
