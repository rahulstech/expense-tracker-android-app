package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.MoneyTransferDetails;
import dreammaker.android.expensetracker.util.Date;
import dreammaker.android.expensetracker.util.Helper;

public class MoneyTransferHistoryAdapter
        extends BaseRecyclerViewListAdapter<MoneyTransferDetails,MoneyTransferHistoryAdapter.MoneyTransferHistoryViewHolder> {

    private static DiffUtil.ItemCallback<MoneyTransferDetails> DIFF_CALLBACK = new DiffUtil.ItemCallback<MoneyTransferDetails>() {

        @Override
        public boolean areItemsTheSame(@NonNull MoneyTransferDetails oldItem, @NonNull MoneyTransferDetails newItem) {
            return null != oldItem && null != newItem && oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MoneyTransferDetails oldItem, @NonNull MoneyTransferDetails newItem) {
            return null != oldItem && oldItem.equalsContent(newItem);
        }
    };

    public MoneyTransferHistoryAdapter(@NonNull Context context) {
        super(context, DIFF_CALLBACK);
    }

    @Override
    protected long getItemId(@NonNull MoneyTransferDetails item) {
        return item.getId();
    }

    @NonNull
    @Override
    public MoneyTransferHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MoneyTransferHistoryViewHolder vh = new MoneyTransferHistoryViewHolder(getLayoutInflater()
                .inflate(R.layout.money_transfer_history_item,parent,false));
        vh.setOnChildClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MoneyTransferHistoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static class MoneyTransferHistoryViewHolder extends RVAViewHolder {
        private static final String DATE_FORMAT = "EEEE, dd-MMMM-yyyy";

        TextView amount;
        TextView when;
        TextView fromAccount;
        TextView toAccount;
        TextView description;
        public View options;

        public MoneyTransferHistoryViewHolder(View itemView) {
            super(itemView);
            amount = findViewById(R.id.amount);
            when = findViewById(R.id.when);
            fromAccount = findViewById(R.id.from_account);
            toAccount = findViewById(R.id.to_account);
            description = findViewById(R.id.description);
            options = findViewById(R.id.options);
            bindChildForClick(options);
        }

        public void bind(MoneyTransferDetails mt) {
            amount.setText(Helper.floatToString(mt.getAmount()));
            setDateText(mt.getWhen());
            fromAccount.setText(mt.getPayer().getAccountName());
            toAccount.setText(mt.getPayee().getAccountName());
            description.setText(mt.getDescription());
        }

        private void setDateText(Date v){
            if (v.isToday())
                when.setText(R.string.today);
            else if (v.isYesterday())
                when.setText(R.string.yesterday);
            else if (v.isTomorrow())
                when.setText(R.string.tomorrow);
            else
                when.setText(v.format(DATE_FORMAT));
        }
    }
}
