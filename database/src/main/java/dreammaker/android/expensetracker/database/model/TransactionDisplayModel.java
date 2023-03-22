package dreammaker.android.expensetracker.database.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.database.Transaction;
import dreammaker.android.expensetracker.database.type.TransactionType;

@Deprecated
public class TransactionDisplayModel {

    @Embedded
    private Transaction transaction;

    @Relation(entity = Account.class, parentColumn = "account_id", entityColumn = "_id")
    private AccountDisplayModel account;

    @Relation(entity = Person.class, parentColumn = "person_id", entityColumn = "_id")
    private PersonDisplayModel person;

    public TransactionDisplayModel(Transaction transaction, AccountDisplayModel account, PersonDisplayModel person) {
        this.transaction = transaction;
        this.account = account;
        this.person = person;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public TransactionType getTransactionType() {
        //return transaction.getTransactionType();
        return null;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public AccountDisplayModel getAccount() {
        return account;
    }

    public void setAccount(AccountDisplayModel account) {
        this.account = account;
    }

    public PersonDisplayModel getPerson() {
        return person;
    }

    public void setPerson(PersonDisplayModel person) {
        this.person = person;
    }
}
