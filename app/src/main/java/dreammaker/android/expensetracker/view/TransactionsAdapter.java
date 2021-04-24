package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.TransactionDetails;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;
import dreammaker.android.expensetracker.util.Helper;

import static dreammaker.android.expensetracker.database.TransactionDetails.TYPE_DEBIT;
import static dreammaker.android.expensetracker.util.Helper.CATEGORY_TRANSACTION;

public class TransactionsAdapter extends BaseRecyclerViewPagedListAdapter<TransactionDetails, TransactionsAdapter.TransactionsViewHolder> implements RVAViewHolder.OnChildClickListener {

    private static final String TAG = "TransactionsAdapter";

    private static DiffUtil.ItemCallback<TransactionDetails> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransactionDetails>() {

        @Override
        public boolean areItemsTheSame(@NonNull TransactionDetails oldItem, @NonNull TransactionDetails newItem) {
            return oldItem.getTransactionId() == newItem.getTransactionId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionDetails oldItem, @NonNull TransactionDetails newItem) {
            return oldItem.equalContents(newItem);
        }
    };

    private int category;

    public TransactionsAdapter(@NonNull Context context){
        this(context, CATEGORY_TRANSACTION);
    }

    @Deprecated
    public TransactionsAdapter(@NonNull Context context, int category) {
        super(context, DIFF_CALLBACK);
        this.category = category;
    }

    @Override
    protected long getItemId(@NonNull TransactionDetails item) {
        return item.getTransactionId();
    }

    @NonNull
    @Override
    public TransactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TransactionsViewHolder vh = new TransactionsViewHolder(getLayoutInflater()
                .inflate(R.layout.transaction_list_item, parent, false), category);
        vh.setOnChildClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionsViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void onChildClick(RecyclerView.ViewHolder vh, View child) {
        if (hasOnItemChildClickListener()){
            getOnItemChildClickListener().onItemChildClicked(this, (TransactionsViewHolder) vh, child);
        }
    }

    public static class TransactionsViewHolder extends RVAViewHolder{

        private static final String DATE_FORMAT = "EEEE, dd-MMMM-yyyy";

        Group contentTransactionDetails;
        Group contentLoading;
        TextView amount;
        TextView date;
        TextView account;
        TextView person;
        TextView description;

        public TransactionsViewHolder(View itemView, int category) {
            super(itemView);
            contentTransactionDetails = findViewById(R.id.content_transaction_details);
            contentLoading = findViewById(R.id.content_loading);
            amount = findViewById(R.id.amount);
            date = findViewById(R.id.date);
            account = findViewById(R.id.account_name);
            person = findViewById(R.id.person_name);
            account.setMovementMethod(new LinkMovementMethod());
            person.setMovementMethod(new LinkMovementMethod());
            description = findViewById(R.id.description);
            findViewById(R.id.options).setOnClickListener(v -> {
                if (hasOnChildClickListener()){
                    getOnChildClickListener().onChildClick(TransactionsViewHolder.this, v);
                }
            });
        }

        public void bind(TransactionDetails t){
            if (Check.isNonNull(t)){
                setAmount(t.getAmount(), t.getType());
                setDateText(t.getDate());
                setAccountName(t.getAccountName());
                setPersonName(t.getPersonName());
                setDescriptionText(t.getDescription());
                setType(t.getType());
                hide(contentLoading);
                show(contentTransactionDetails);
            }
            else {
                hide(contentTransactionDetails);
                show(contentLoading);
            }
        }

        private void setAmount(float amountValue, int type){
            CharSequence cs = Helper.floatToString(amountValue);
            if (TYPE_DEBIT == type) amount.setText("+"+cs);
            else amount.setText("-"+cs);
        }

        private void setDateText(Date v){
            if (v.isToday())
                date.setText(R.string.today);
            else if (v.isYesterday())
                date.setText(R.string.yesterday);
            else if (v.isTomorrow())
                date.setText(R.string.tomorrow);
            else
                date.setText(v.format(DATE_FORMAT));
        }

        private void setType(int type){
            if (TYPE_DEBIT == type)
                amount.setTextColor(getRoot().getContext().getResources().getColor(R.color.text_green));
            else
                amount.setTextColor(getRoot().getContext().getResources().getColor(R.color.text_red));
        }

        private void setDescriptionText(CharSequence text){
            if (Check.isEmptyString(text))hide(description);
            else {
                show(description);
                description.setText(text);
            }
        }

        private void setAccountName(CharSequence accountName){
            account.setText(newClickableText(accountName, clickableSpanForAccount));
        }

        private void setPersonName(CharSequence personName) {
            if (Check.isEmptyString(personName))
                person.setText(newClickableText(person.getContext().getString(R.string.item_none),
                        clickableSpanForPerson));
            else
                person.setText(newClickableText(personName, clickableSpanForPerson));
        }

        private SpannableString newClickableText(CharSequence text, ClickableSpan span){
            if (Check.isEmptyString(text)) return null;
            SpannableString content = new SpannableString(text);
            content.setSpan(span, 0, text.length(), SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
            return content;
        }

        private ClickableSpan clickableSpanForPerson = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (hasOnChildClickListener())
                    getOnChildClickListener().onChildClick(TransactionsViewHolder.this, person);
            }
        };

        private ClickableSpan clickableSpanForAccount = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (hasOnChildClickListener())
                    getOnChildClickListener().onChildClick(TransactionsViewHolder.this, account);
            }
        };

        private void hide(View v){ v.setVisibility(View.INVISIBLE); }

        private void show(View v){ v.setVisibility(View.VISIBLE); }
    }
}
