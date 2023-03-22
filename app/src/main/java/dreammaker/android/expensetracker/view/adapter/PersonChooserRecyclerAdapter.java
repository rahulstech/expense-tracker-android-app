package dreammaker.android.expensetracker.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonDisplayModel;

public class PersonChooserRecyclerAdapter
        extends BaseCheckableItemRecyclerViewListAdapter<PersonDisplayModel, PersonChooserRecyclerAdapter.PersonChooserViewHolder> {

    private static DiffUtil.ItemCallback<PersonDisplayModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<PersonDisplayModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull PersonDisplayModel oldItem, @NonNull PersonDisplayModel newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull PersonDisplayModel oldItem, @NonNull PersonDisplayModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    public PersonChooserRecyclerAdapter(@NonNull Context context) {
        super(context, DIFF_CALLBACK);
        setHasStableIds(true);
    }

    @Override
    protected PersonChooserViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        return new PersonChooserViewHolder(inflater.inflate(R.layout.two_line_checkable_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull PersonChooserViewHolder holder, int position) {
        PersonDisplayModel item = getItem(position);
        holder.bind(item);
        holder.setChecked(isChecked(position));
    }

    @Override
    public long getItemId(int position) {
        PersonDisplayModel item = getItem(position);
        if (null == item) return RecyclerView.NO_ID;
        return item.getId();
    }

    public static class PersonChooserViewHolder extends BaseCheckableItemRecyclerViewListAdapter.BaseCheckableItemViewHolder<PersonDisplayModel> {

        private RadioButton radio;
        private TextView line1;
        private TextView line2;

        public PersonChooserViewHolder(@NonNull View itemView) {
            super(itemView);
            radio = findViewById(R.id.radio);
            line1 = findViewById(R.id.line1);
            line2 = findViewById(R.id.line2);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nullable PersonDisplayModel item, @Nullable Object payload) {
            if (null == item) {
                line1.setText(null);
                line2.setText(null);
                radio.setVisibility(View.INVISIBLE);
            }
            else {
                line1.setText(item.getName());
                line2.setText(item.getAmountDue().toPlainString());
                radio.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void setChecked(boolean checked) {
            radio.setChecked(checked);
        }
    }
}
