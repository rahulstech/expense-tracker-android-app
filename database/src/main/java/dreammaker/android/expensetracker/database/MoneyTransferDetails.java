package dreammaker.android.expensetracker.database;

import java.math.BigDecimal;

import androidx.room.Ignore;
import androidx.room.Relation;
import dreammaker.android.expensetracker.database.type.Date;

@Deprecated
public class MoneyTransferDetails extends MoneyTransfer {

    @Relation(entity =  Account.class, parentColumn = "payee_account_id", entityColumn = "_id")
    private Account payee;

    @Relation(entity = Account.class, parentColumn = "payer_account_id", entityColumn = "_id")
    private Account payer;

    @Ignore
    @Deprecated
    public MoneyTransferDetails(long id, Date when, float amount, long payee_account_id, long payer_account_id, String description) {
        super(id, when, amount, payee_account_id, payer_account_id, description);
    }

    public MoneyTransferDetails(long id, Date when, BigDecimal totalAmount, long payee_account_id, long payer_account_id, String description) {
        super(id, when, totalAmount, payee_account_id, payer_account_id, description);
    }

    public Account getPayee() {
        return payee;
    }

    public void setPayee(Account payee) {
        this.payee = payee;
    }

    public Account getPayer() {
        return payer;
    }

    public void setPayer(Account payer) {
        this.payer = payer;
    }

    public boolean equalsContent(MoneyTransferDetails mtd) {
        if (super.equalsContent(mtd)) {
            return payee.equalContents(mtd.payee)
                    && payer.equalContents(mtd.payer);
        }
        return false;
    }
}