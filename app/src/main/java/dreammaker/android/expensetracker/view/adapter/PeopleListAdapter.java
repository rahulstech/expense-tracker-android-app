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
import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonDisplayModel;

public class PeopleListAdapter extends BaseClickableItemRecyclerViewListAdapter<PersonDisplayModel, PeopleListAdapter.PersonViewModel> {

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

    public PeopleListAdapter(@NonNull Context context) {
        super(context, DIFF_CALLBACK,true);
    }

    @Override
    protected PersonViewModel onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        return new PersonViewModel(inflater.inflate(R.layout.list_item_two_lines,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewModel holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    protected List<PersonDisplayModel> onFilter(@Nullable CharSequence constraint) {
        final List<PersonDisplayModel> original = getOriginalItems();
        final List<PersonDisplayModel> current = getCurrentList();
        if (null == original || original.isEmpty() || TextUtils.isEmpty(constraint)) return original;
        if (null == current || current.isEmpty()) return null;
        String phrase = constraint.toString();
        ArrayList<PersonDisplayModel> values = new ArrayList<>();
        for (PersonDisplayModel a : current) {
            String name = a.getName();
            if (name.contains(phrase)) {
                values.add(a);
            }
        }
        return values;
    }

    public static class PersonViewModel extends BaseClickableItemRecyclerViewListAdapter.BaseClickableItemViewHolder<PersonDisplayModel> {

        TextView line1;
        TextView line2;
        View options;

        public PersonViewModel(@NonNull View itemView) {
            super(itemView);
            line1 = findViewById(R.id.line1);
            line2 = findViewById(R.id.line2);
            options = findViewById(R.id.options);
            itemView.setOnClickListener(this);
            options.setOnClickListener(this);
        }

        @Override
        public void bind(@Nullable PersonDisplayModel person, @Nullable Object payload) {
            if (null != person){
                line1.setText(person.getName());
                BigDecimal due = person.calculateDue();
                BigDecimal borrow = person.calculateBorrow();
                SpannableString txtDue = SpannableString.valueOf(due.toString());
                txtDue.setSpan(new ForegroundColorSpan(getColor(R.color.text_green)),0,txtDue.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                SpannableString txtBorrow = SpannableString.valueOf(borrow.toString());
                txtBorrow.setSpan(new ForegroundColorSpan(getColor(R.color.text_red)),0,txtBorrow.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(getString(R.string.label_total_due)).append(": ").append(txtDue);
                builder.append("\n");
                builder.append(getString(R.string.label_total_borrow)).append(": ").append(txtBorrow);
                line2.setText(builder);
            }
        }
    }
}
