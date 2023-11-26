package dreammaker.android.expensetracker.database.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import dreammaker.android.expensetracker.database.Converters;

@Deprecated
public class AccountDisplayModel implements Cloneable {

    private long id;

    private String name;

    @TypeConverters(Converters.class)
    private BigDecimal balance;

    @Ignore
    AccountDisplayModel(long id, String name, BigDecimal balance) {
        this.id = id;
        this.name = name;
        setBalance(balance);
    }

    public AccountDisplayModel() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance.setScale(2, RoundingMode.HALF_DOWN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountDisplayModel)) return false;
        AccountDisplayModel that = (AccountDisplayModel) o;
        return id == that.id && name.equals(that.name) && balance.equals(that.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, balance);
    }

    @Override
    public String toString() {
        return "AccountDisplayModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }

    @NonNull
    @Override
    public AccountDisplayModel clone() {
        return new AccountDisplayModel(id,name,new BigDecimal(balance.toString()));
    }
}
