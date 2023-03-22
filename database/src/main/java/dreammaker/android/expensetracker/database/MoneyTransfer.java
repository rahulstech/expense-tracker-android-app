package dreammaker.android.expensetracker.database;

import org.jetbrains.annotations.NotNull;

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
import dreammaker.android.expensetracker.database.type.Date;

import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.AMOUNT;

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
@Deprecated
public class MoneyTransfer implements Cloneable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo( typeAffinity = ColumnInfo.INTEGER)
    private long id;
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    @NonNull
    @TypeConverters(Converters.class)
    private Date when;
    @ColumnInfo(name = AMOUNT, typeAffinity = ColumnInfo.REAL, defaultValue = "0")
    @NonNull
    private BigDecimal totalAmount;
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    @NonNull
    private long payee_account_id;
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    @NonNull
    private long payer_account_id;
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    private String description;

    @Ignore
    @Deprecated
    public MoneyTransfer(long _id, @NotNull Date when, float amount, long payee_account_id, long payer_account_id, String description) {
        this.id = _id;
        this.when = when;
        this.payee_account_id = payee_account_id;
        this.payer_account_id = payer_account_id;
        this.description = description;
    }

    public MoneyTransfer(long _id, @NotNull Date when, BigDecimal totalAmount, long payee_account_id, long payer_account_id, String description) {
        this.id = _id;
        this.when = when;
        this.totalAmount = totalAmount;
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

    @Deprecated
    public float getAmount() {
        return null == totalAmount ? 0 : totalAmount.floatValue();
    }

    @Deprecated
    public void setAmount(float amount) {
        setTotalAmount(new BigDecimal(amount).setScale(2, RoundingMode.HALF_DOWN));
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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
                ", amount=" + totalAmount +
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
                    totalAmount.compareTo(that.totalAmount) == 0 &&
                    payee_account_id == that.payee_account_id &&
                    payer_account_id == that.payer_account_id &&
                    when.equals(that.when) &&
                    Objects.deepEquals(description, that.description);
        }
        return false;
    }

    @NonNull
    @Override
    public MoneyTransfer clone() {
        return new MoneyTransfer(this.id,this.when.clone(),
                this.totalAmount,this.payee_account_id,this.payer_account_id,this.description);
    }
}
