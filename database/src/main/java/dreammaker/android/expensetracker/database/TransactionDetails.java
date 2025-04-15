package dreammaker.android.expensetracker.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Relation;

import java.util.Objects;

public class TransactionDetails extends Transaction {
    @Relation(parentColumn = "account_id", entityColumn = "_id")
    private Account account = null;
    @Relation(parentColumn = "person_id", entityColumn = "_id")
    private Person person = null;

    public TransactionDetails(long transactionId, long accountId, Long personId, float amount, int type, @NonNull Date date, boolean deleted, String description) {
        super(transactionId, accountId, personId, amount, type, date, deleted, description);
    }

    @NonNull
    public String getAccountName() {
        return account.getAccountName();
    }

    public String getPersonName() {
        return null == person ? null : person.getPersonName();
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public boolean equalContents(@Nullable TransactionDetails td) {
        if (super.equalContents(td)) {
            return Objects.equals(this.getAccountName(),td.getAccountName())
                    && Objects.equals(this.getPersonName(),td.getPersonName());
        }
        return false;
    }
}
