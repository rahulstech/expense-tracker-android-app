package dreammaker.android.expensetracker.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.AccountDisplayModel;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;

public class AccountChooserRecyclerAdapter
        extends BaseCheckableItemRecyclerViewListAdapter<AccountDisplayModel, AccountChooserRecyclerAdapter.AccountChooserViewHolder> {

    private static DiffUtil.ItemCallback<AccountDisplayModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<AccountDisplayModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull AccountDisplayModel oldItem, @NonNull AccountDisplayModel newItem) {
            return oldItem.getId() == oldItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AccountDisplayModel oldItem, @NonNull AccountDisplayModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    public AccountChooserRecyclerAdapter(@NonNull Context context) {
        super(context,DIFF_CALLBACK);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        AccountDisplayModel item = getItem(position);
        if (null == item) return NO_ID;
        return item.getId();
    }

    @Override
    protected AccountChooserViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        return new AccountChooserViewHolder(inflater.inflate(R.layout.two_line_checkable_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountChooserViewHolder holder, int position) {
        AccountDisplayModel item = getItem(position);
        holder.bind(item);
        holder.setChecked(isChecked(position));
    }

    public static class AccountChooserViewHolder extends BaseCheckableItemViewHolder<AccountDisplayModel> {

        private RadioButton radio;
        private TextView line1;
        private TextView line2;

        public AccountChooserViewHolder(@NonNull View itemView) {
            super(itemView);
            radio = findViewById(R.id.radio);
            line1 = findViewById(R.id.line1);
            line2 = findViewById(R.id.line2);
            itemView.setOnClickListener(this);
        }

        public void bind(@Nullable AccountDisplayModel item, @Nullable Object payload) {
            if (null != item) {
                radio.setVisibility(View.VISIBLE);
                line1.setText(item.getName());
                line2.setText(item.getBalance().toPlainString());
            }
            else {
                radio.setVisibility(View.INVISIBLE);
                line1.setText(null);
                line2.setText(null);
            }
        }

        @Override
        public void setChecked(boolean checked) {
            radio.setChecked(checked);
        }
    }
}
