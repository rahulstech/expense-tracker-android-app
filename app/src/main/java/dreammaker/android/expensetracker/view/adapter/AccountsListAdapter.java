package dreammaker.android.expensetracker.view.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.AccountDisplayModel;

public class AccountsListAdapter extends BaseClickableItemRecyclerViewListAdapter<AccountDisplayModel, AccountsListAdapter.AccountViewHolder>{

    private static DiffUtil.ItemCallback<AccountDisplayModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<AccountDisplayModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull AccountDisplayModel oldItem, @NonNull AccountDisplayModel newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AccountDisplayModel oldItem, @NonNull AccountDisplayModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    public AccountsListAdapter(@NonNull Context context) {
        super(context, DIFF_CALLBACK,true);
    }

    @Override
    protected AccountViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        AccountViewHolder vh = new AccountViewHolder(inflater.inflate(R.layout.list_item_two_lines,parent,false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    protected List<AccountDisplayModel> onFilter(@Nullable CharSequence constraint) {
        final List<AccountDisplayModel> original = getOriginalItems();
        final List<AccountDisplayModel> current = getCurrentList();
        if (null == original || original.isEmpty() || TextUtils.isEmpty(constraint)) return original;
        if (null == current || current.isEmpty()) return null;
        String phrase = constraint.toString();
        ArrayList<AccountDisplayModel> values = new ArrayList<>();
        for (AccountDisplayModel a : current) {
            String name = a.getName();
            if (name.contains(phrase)) {
                values.add(a);
            }
        }
        return values;
    }

    public static class AccountViewHolder extends BaseClickableItemViewHolder<AccountDisplayModel> {

        TextView line1;
        TextView line2;
        View btnOptions;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            line1 = findViewById(R.id.line1);
            line2 = findViewById(R.id.line2);
            btnOptions = findViewById(R.id.options);
            itemView.setOnClickListener(this);
            btnOptions.setOnClickListener(this);
        }

        @Override
        public void bind(@Nullable AccountDisplayModel item, @Nullable Object payload) {
            if (null != item) {
                line1.setText(item.getName());
                BigDecimal balance = item.getBalance();
                SpannableString txtBalance = SpannableString.valueOf(balance.toPlainString());
                int txtBalanceColor = balance.compareTo(BigDecimal.ZERO) > 0 ? getColor(R.color.text_green) : getColor(R.color.text_red);
                txtBalance.setSpan(new ForegroundColorSpan(txtBalanceColor),0,txtBalance.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(getString(R.string.label_total_balance)).append(": ").append(txtBalance);
                line2.setText(builder);
            }
            else {
                // TODO: show loading

            }
        }
    }
}
