package dreammaker.android.expensetracker.view;

import android.content.Context;

import androidx.annotation.NonNull;
import dreammaker.android.expensetracker.database.AboutAccount;
import dreammaker.android.expensetracker.database.Account;

public class AccountsSpinnerAdapter extends BaseSpinnerAdapter<Account> {

    public AccountsSpinnerAdapter(Context context) {
        super(context);
    }

    @Override
    protected long getItemId(@NonNull Account item) {
        return item.getAccountId();
    }

    @Override
    protected void onBindViewHolder(SpinnerViewHolder vh, int position) {
        vh.setContentText(getItem(position).getAccountName());
    }
}
