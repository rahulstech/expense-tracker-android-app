package dreammaker.android.expensetracker.database.model;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;
import dreammaker.android.expensetracker.database.type.TransactionType;

@Deprecated
public class TransactionHistoryDisplayModel implements Cloneable {

    @Embedded
    private TransactionHistory transaction;

    @Relation(entity = Account.class, entityColumn = "id", parentColumn = "payeeAccountId")
    @Nullable
    private AccountDisplayModel payeeAccount;

    @Relation(entity = Account.class, entityColumn = "id", parentColumn = "payerAccountId")
    @Nullable
    private AccountDisplayModel payerAccount;

    @Relation(entity = Person.class, entityColumn = "id", parentColumn = "payeePersonId")
    @Nullable
    private PersonDisplayModel payeePerson;

    @Relation(entity = Person.class, entityColumn = "id", parentColumn = "payerPersonId")
    @Nullable
    private PersonDisplayModel payerPerson;

    @Ignore
    TransactionHistoryDisplayModel(TransactionHistory transaction, @Nullable AccountDisplayModel payeeAccount, @Nullable AccountDisplayModel payerAccount, @Nullable PersonDisplayModel payeePerson, @Nullable PersonDisplayModel payerPerson) {
        this.transaction = transaction;
        this.payeeAccount = payeeAccount;
        this.payerAccount = payerAccount;
        this.payeePerson = payeePerson;
        this.payerPerson = payerPerson;
    }

    public TransactionHistoryDisplayModel() {}

    public TransactionHistory getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionHistory transaction) {
        this.transaction = transaction;
    }

    public long getTransactionId() {
        return transaction.getId();
    }

    @NonNull
    public TransactionType getType() {
        return transaction.getType();
    }

    @Nullable
    public AccountDisplayModel getPayeeAccount() {
        return payeeAccount;
    }

    public void setPayeeAccount(@Nullable AccountDisplayModel payeeAccount) {
        this.payeeAccount = payeeAccount;
    }

    @Nullable
    public AccountDisplayModel getPayerAccount() {
        return payerAccount;
    }

    public void setPayerAccount(@Nullable AccountDisplayModel payerAccount) {
        this.payerAccount = payerAccount;
    }

    @Nullable
    public PersonDisplayModel getPayeePerson() {
        return payeePerson;
    }

    public void setPayeePerson(@Nullable PersonDisplayModel payeePerson) {
        this.payeePerson = payeePerson;
    }

    @Nullable
    public PersonDisplayModel getPayerPerson() {
        return payerPerson;
    }

    public void setPayerPerson(@Nullable PersonDisplayModel payerPerson) {
        this.payerPerson = payerPerson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionHistoryDisplayModel)) return false;
        TransactionHistoryDisplayModel that = (TransactionHistoryDisplayModel) o;
        return transaction.equals(that.transaction) && Objects.equals(payeeAccount, that.payeeAccount) && Objects.equals(payerAccount, that.payerAccount) && Objects.equals(payeePerson, that.payeePerson) && Objects.equals(payerPerson, that.payerPerson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transaction, payeeAccount, payerAccount, payeePerson, payerPerson);
    }

    @NonNull
    @Override
    public TransactionHistoryDisplayModel clone() {
        TransactionHistory tCopy = transaction.clone();
        AccountDisplayModel peaCopy = payeeAccount.clone();
        AccountDisplayModel praCopy = payerAccount.clone();
        PersonDisplayModel pepCopy = payeePerson.clone();
        PersonDisplayModel prpCopy = payerPerson.clone();
        return new TransactionHistoryDisplayModel(tCopy,peaCopy,praCopy,pepCopy,prpCopy);
    }
}
