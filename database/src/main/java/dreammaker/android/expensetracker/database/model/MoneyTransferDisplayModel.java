package dreammaker.android.expensetracker.database.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.database.MoneyTransfer;

public class MoneyTransferDisplayModel {

    @Embedded
    private MoneyTransfer moneyTransfer;

    @Relation(entity = Account.class, parentColumn = "payee_account_id", entityColumn = "_id")
    private AccountDisplayModel payeeAccount;

    @Relation(entity = Account.class, parentColumn = "payer_account_id", entityColumn = "_id")
    private AccountDisplayModel payerAccount;

    public MoneyTransferDisplayModel(MoneyTransfer moneyTransfer, AccountDisplayModel payeeAccount, AccountDisplayModel payerAccount) {
        this.moneyTransfer = moneyTransfer;
        this.payeeAccount = payeeAccount;
        this.payerAccount = payerAccount;
    }

    public MoneyTransfer getMoneyTransfer() {
        return moneyTransfer;
    }

    public void setMoneyTransfer(MoneyTransfer moneyTransfer) {
        this.moneyTransfer = moneyTransfer;
    }

    public AccountDisplayModel getPayeeAccount() {
        return payeeAccount;
    }

    public void setPayeeAccount(AccountDisplayModel payeeAccount) {
        this.payeeAccount = payeeAccount;
    }

    public AccountDisplayModel getPayerAccount() {
        return payerAccount;
    }

    public void setPayerAccount(AccountDisplayModel payerAccount) {
        this.payerAccount = payerAccount;
    }
}
