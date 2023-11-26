package dreammaker.android.expensetracker.database.model;

import java.time.LocalDate;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.Relation;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;

@SuppressWarnings("unused")
public class TransactionHistoryModel {

    private Long id;

    private Long payeeAccountId;

    private Long payerAccountId;

    private Long payeePersonId;

    private Long payerPersonId;

    private TransactionType type;

    private Currency amount;

    private LocalDate when;

    private String description;

    @Relation(entity = Account.class, parentColumn = "payeeAccountId", entityColumn = "id", projection = {"id","name"})
    private AccountModel payeeAccount;

    @Relation(entity = Account.class, parentColumn = "payerAccountId", entityColumn = "id", projection = {"id","name"})
    private AccountModel payerAccount;

    @Relation(entity = Person.class, parentColumn = "payeePersonId", entityColumn = "id", projection = {"id","firstName","lastName"})
    private PersonModel payeePerson;

    @Relation(entity = Person.class, parentColumn = "payerPersonId", entityColumn = "id", projection = {"id","firstName","lastName"})
    private PersonModel payerPerson;

    public TransactionHistoryModel() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Currency getAmount() {
        return amount;
    }

    public void setAmount(Currency amount) {
        this.amount = amount;
    }

    public LocalDate getWhen() {
        return when;
    }

    public void setWhen(LocalDate when) {
        this.when = when;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AccountModel getPayeeAccount() {
        return payeeAccount;
    }

    public void setPayeeAccount(AccountModel payeeAccount) {
        this.payeeAccount = payeeAccount;
    }

    public AccountModel getPayerAccount() {
        return payerAccount;
    }

    public void setPayerAccount(AccountModel payerAccount) {
        this.payerAccount = payerAccount;
    }

    public PersonModel getPayeePerson() {
        return payeePerson;
    }

    public void setPayeePerson(PersonModel payeePerson) {
        this.payeePerson = payeePerson;
    }

    public PersonModel getPayerPerson() {
        return payerPerson;
    }

    public void setPayerPerson(PersonModel payerPerson) {
        this.payerPerson = payerPerson;
    }

    @NonNull
    @Override
    public String toString() {
        return "TransactionHistoryModel{" +
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
        if (!(o instanceof TransactionHistoryModel)) return false;
        TransactionHistoryModel that = (TransactionHistoryModel) o;
        return Objects.equals(id, that.id) && Objects.equals(payeeAccountId, that.payeeAccountId) && Objects.equals(payerAccountId, that.payerAccountId) && Objects.equals(payeePersonId, that.payeePersonId) && Objects.equals(payerPersonId, that.payerPersonId) && type == that.type && Objects.equals(amount, that.amount) && Objects.equals(when, that.when) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, payeeAccountId, payerAccountId, payeePersonId, payerPersonId, type, amount, when, description);
    }
}
