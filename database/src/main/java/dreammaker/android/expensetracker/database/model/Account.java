package dreammaker.android.expensetracker.database.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import dreammaker.android.expensetracker.database.Converters;

import static androidx.room.ColumnInfo.TEXT;

@Entity(tableName = "accounts")
public class Account implements Cloneable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long id;

    @NonNull
    private String name;

    @ColumnInfo(typeAffinity = TEXT, defaultValue = "0")
    @NonNull
    @TypeConverters(Converters.class)
    private BigDecimal balance;

    @Ignore
    public Account(long id, String name, BigDecimal balance) {
        this.id = id;
        this.name = name;
        setBalance(balance);
    }

    public Account() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(@NonNull BigDecimal balance) {
        this.balance = balance.setScale(2, RoundingMode.HALF_DOWN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return id == account.id && name.equals(account.name) && balance.equals(account.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, balance);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }

    @NonNull
    @Override
    public Account clone() {
        return new Account(id,name,new BigDecimal(balance.toString()));
    }
}
