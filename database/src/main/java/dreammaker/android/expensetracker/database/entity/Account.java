package dreammaker.android.expensetracker.database.entity;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import dreammaker.android.expensetracker.database.type.Currency;

@Entity(tableName = "accounts")
@SuppressWarnings("unused")
public class Account {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    @NonNull
    private Currency balance;

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
    public Currency getBalance() {
        return balance;
    }

    public void setBalance(@NonNull Currency balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
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

    @NonNull
    public Account copy() {
        Account copy = new Account();
        copy.id = id;
        copy.name = name;
        copy.balance = balance;
        return copy;
    }
}
