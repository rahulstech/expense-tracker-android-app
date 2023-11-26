package dreammaker.android.expensetracker.database.model;

import java.util.Objects;

import androidx.annotation.NonNull;
import dreammaker.android.expensetracker.database.type.Currency;

@SuppressWarnings("unused")
public class AccountModel {

    private Long id;

    private String name;

    private Currency balance;

    private Integer usageCount;

    public AccountModel() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Currency getBalance() {
        return balance;
    }

    public void setBalance(Currency balance) {
        this.balance = balance;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    @NonNull
    @Override
    public String toString() {
        return "AccountModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", usageCount=" + usageCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountModel)) return false;
        AccountModel that = (AccountModel) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(balance, that.balance) && Objects.equals(usageCount, that.usageCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, balance, usageCount);
    }
}
