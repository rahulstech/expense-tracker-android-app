package dreammaker.android.expensetracker.database;

import java.math.BigDecimal;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Ignore;
import androidx.room.Relation;
import dreammaker.android.expensetracker.database.type.Date;

@Deprecated
public class TransactionDetails extends Transaction {
    @Relation(parentColumn = "account_id", entityColumn = "_id")
    private Account account = null;
    @Relation(parentColumn = "person_id", entityColumn = "_id")
    private Person person = null;

    @Ignore
    @Deprecated
    public TransactionDetails(long transactionId, long accountId, Long personId, float amount, int type, @NonNull Date date, boolean deleted, String description) {
        super(transactionId, accountId, personId, BigDecimal.valueOf(amount), type, date, deleted, description);
    }

    public TransactionDetails(long transactionId, long accountId, Long personId, BigDecimal totalAmount, int type, @NonNull Date date, boolean deleted, String description) {
        super(transactionId, accountId, personId, totalAmount, type, date, deleted, description);
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
        if (super.equals(td)) {
            return Objects.deepEquals(this.getAccountName(), td.getAccountName())
                    && Objects.deepEquals(this.getPersonName(),td.getPersonName());
        }
        return false;
    }
}
