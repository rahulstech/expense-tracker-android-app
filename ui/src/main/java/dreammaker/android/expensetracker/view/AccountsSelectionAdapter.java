package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.util.Check;

public class AccountsSelectionAdapter extends AbsSelectionListAdapter<Account, AccountsSelectionAdapter.AccountSelectionViewHolder> {

    public AccountsSelectionAdapter(Context context) {
        super(context);
    }

    @Override
    public boolean onMatch(@Nullable Account item, @NonNull String key) {
        return null != item && item.getAccountName().toLowerCase().contains(key.toLowerCase());
    }

    @Override
    protected AccountSelectionViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        return new AccountSelectionViewHolder(getLayoutInflater()
                .inflate(android.R.layout.simple_list_item_multiple_choice,
                        parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull AccountSelectionViewHolder vh, int position, boolean checked) {
        final Account account = getItem(position);
        vh.bind(account);
    }

    @Override
    protected long getItemId(@NonNull Account item) {
        return item.getAccountId();
    }

    public static class AccountSelectionViewHolder extends ViewHolder{
        TextView text1;

        public AccountSelectionViewHolder(View root) {
            super(root);
            text1 = findViewById(android.R.id.text1);
        }

        public void bind(Account account){
            if (Check.isNonNull(account)) text1.setText(account.getAccountName());
            else text1.setText(null);
        }
    }
}
