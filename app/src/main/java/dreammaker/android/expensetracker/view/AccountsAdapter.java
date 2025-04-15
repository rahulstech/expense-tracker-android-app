package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.AboutAccount;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Helper;

public class AccountsAdapter extends BaseRecyclerViewListAdapterFilterable<AboutAccount, AccountsAdapter.AccountViewHolder> {
    private static DiffUtil.ItemCallback<AboutAccount> DIFF_CALLBACK = new DiffUtil.ItemCallback<AboutAccount>() {
        @Override
        public boolean areItemsTheSame(@NonNull AboutAccount oldItem, @NonNull AboutAccount newItem) {
            return oldItem.getAccountId() == newItem.getAccountId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AboutAccount oldItem, @NonNull AboutAccount newItem) {
            return oldItem.equalContents(newItem);
        }
    };

    public AccountsAdapter(Context context){
        super(context, DIFF_CALLBACK);
    }

    @Override
    protected long getItemId(@NonNull AboutAccount item) {
        return item.getAccountId();
    }

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AccountViewHolder vh = new AccountViewHolder(getLayoutInflater().inflate(R.layout.account_list_item, parent, false));
        vh.setOnChildClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(AccountViewHolder vh, int adapterPosition) {
        vh.bind(getItem(adapterPosition));
    }

    @Override
    public boolean onMatch(@Nullable AboutAccount item, @NonNull String key) {
        return item.getAccountName().toLowerCase().contains(key.toLowerCase());
    }

    public static class AccountViewHolder extends RVAViewHolder{
        TextView account_name;
        TextView balance;
        public ImageView options;

        AccountViewHolder(View root){
            super(root);
            account_name = findViewById(R.id.from_account);
            balance = findViewById(R.id.balance);
            options = findViewById(R.id.options);
            bindChildForClick(options);
            bindChildForClick(root);
        }

        public void bind(AboutAccount account){
            if (Check.isNonNull(account)){
                account_name.setText(account.getAccountName());
                balance.setText(Helper.floatToString(account.getBalance()));
            }
        }
    }
}
