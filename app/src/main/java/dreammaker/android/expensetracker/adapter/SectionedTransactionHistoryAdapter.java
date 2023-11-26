package dreammaker.android.expensetracker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;

@SuppressWarnings("unused")
public class SectionedTransactionHistoryAdapter
        extends SectionedListAdapter<LocalDate, TransactionHistoryModel,
        SectionedTransactionHistoryAdapter.HeaderViewHolder, SectionedTransactionHistoryAdapter.ChildViewHolder> {

    public static final int HEADER_DATE = 1;

    public static final int HEADER_MONTH = 2;

    private static final ItemCallback<LocalDate,TransactionHistoryModel> CALLBACK
            = new ItemCallback<LocalDate, TransactionHistoryModel>() {
        @Override
        public boolean isSameHeader(@NonNull LocalDate oldHeader, @NonNull LocalDate newHeader) {
            return oldHeader.compareTo(newHeader) == 0;
        }

        @Override
        public boolean isSameChild(@NonNull TransactionHistoryModel oldChild, @NonNull TransactionHistoryModel newChild) {
            return oldChild.getId().longValue() == newChild.getId().longValue();
        }

        @Override
        public boolean isHeaderContentSame(@NonNull LocalDate oldHeader, @NonNull LocalDate newHeader) {
            return oldHeader.compareTo(newHeader) == 0;
        }

        @Override
        public boolean isChildContentSame(@NonNull TransactionHistoryModel oldChild, @NonNull TransactionHistoryModel newChild) {
            return oldChild.equals(newChild);
        }
    };

    private int mHeaderType = HEADER_DATE;

    public SectionedTransactionHistoryAdapter(@NonNull Context context) {
        super(context, CALLBACK);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setHeaderType(int type) {
        mHeaderType = type;
        notifyDataSetChanged();
    }

    public int getHeaderType() {
        return mHeaderType;
    }

    @NonNull
    @Override
    protected HeaderViewHolder onCreateHeaderViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_transaction_history_item_header,parent,false);
        HeaderViewHolder holder = new HeaderViewHolder(view);
        holder.setHeaderType(mHeaderType);
        return holder;
    }

    @NonNull
    @Override
    protected ChildViewHolder onCreateChildViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_transaction_history_item_child,parent,false);
        return new ChildViewHolder(view);
    }

    @Override
    protected void onBindHeaderViewHolder(@NonNull HeaderViewHolder holder, int adapterPosition) {
        holder.bind(getData(adapterPosition));
    }

    @Override
    protected void onBindChildViewHolder(@NonNull ChildViewHolder holder, int adapterPosition) {
        holder.bind(getData(adapterPosition));
    }

    @Nullable
    @Override
    protected List<TransactionHistoryModel> onBeforeCreateSections(@NonNull List<TransactionHistoryModel> list) {
        // TODO: implement necessary filter
        return list;
    }

    @NonNull
    @Override
    protected LocalDate onCreateHeaderFromChild(@NonNull TransactionHistoryModel child) {
        LocalDate when = child.getWhen();
        return LocalDate.from(when);
    }

    public static class HeaderData {

        private LocalDate date;

        private Month month;

        public HeaderData() {}

        public CharSequence getDisplayText() {
            return null;
        }
    }

    public static class HeaderViewHolder extends BaseViewHolder<LocalDate> {

        static final DateTimeFormatter FORMATTER_DATE_WITH_WEEK_DAY = DateTimeFormatter.ofPattern("EEEE, dd-MMMM-yyyy");

        static final DateTimeFormatter FORMATTER_MONTH = DateTimeFormatter.ofPattern("EEEE, dd-MMMM-yyyy");


        TextView text1;
        int mHeaderType;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = findViewById(R.id.text1);
        }

        public void setHeaderType(int type) {
            mHeaderType = type;
        }

        @Override
        protected void onBindNull() {
            text1.setText(null);
        }

        @Override
        protected void onBindNonNull(@NonNull LocalDate item) {
            String headerText;
            if (mHeaderType == HEADER_DATE) {
                headerText = item.format(FORMATTER_DATE_WITH_WEEK_DAY);
            }
            else {
                headerText = item.format(FORMATTER_MONTH);
            }
            text1.setText(headerText);
        }
    }

    public static class ChildViewHolder extends BaseViewHolder<TransactionHistoryModel> {

        ImageView logoPrimary;
        ImageView logoSecondary;
        TextView text1;
        TextView text2;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            logoPrimary = findViewById(R.id.logoPrimary);
            logoPrimary = findViewById(R.id.logoSecondary);
            text1 = findViewById(R.id.text1);
            text2 = findViewById(R.id.text2);
        }

        @Override
        protected void onBindNull() {
            text1.setText(null);
            text2.setText(null);
            logoPrimary.setImageDrawable(null);
            logoSecondary.setImageDrawable(null);
        }

        @Override
        protected void onBindNonNull(@NonNull TransactionHistoryModel item) {
            text1.setText(getDescriptionText(item));
            text2.setText(getAmountText(item));
            logoPrimary.setImageDrawable(getPrimaryLogo(item));
            logoSecondary.setImageDrawable(getSecondaryLogo(item));
        }

        CharSequence getDescriptionText(@NonNull TransactionHistoryModel item) {
            String description = item.getDescription();
            if (!TextUtils.isEmpty(description)) {
                return description;
            }
            /*
            TransactionType  type = item.getType();
            PersonModel personPayer = item.getPayerPerson();
            AccountModel accountPayer = item.getPayerAccount();
            PersonModel personPayee = item.getPayeePerson();
            AccountModel accountPayee = item.getPayeeAccount();
            String unknown = getContext().getString(R.string.text_unknown);
            switch (type) {
                case DUE: {
                    String payee = getPersonDisplayNameOrDefault(personPayee,unknown);
                    String payer = getAccountNameOrDefault(accountPayer,unknown);
                    return getContext().getString(R.string.text_transaction_history_description_due,payee,payer);
                }
                case BORROW:
                case PAY_DUE: {
                    String payee = getAccountNameOrDefault(accountPayee,unknown) ;
                    String payer = getPersonDisplayNameOrDefault(personPayer,unknown);
                    return getContext().getString(R.string.text_amount_received,payer,payee);
                }
                case MONEY_TRANSFER: {
                    String payee = getAccountNameOrDefault(accountPayee,unknown) ;
                    String payer = getAccountNameOrDefault(accountPayer,unknown);
                    return getContext().getString(R.string.text_amount_received,payer,payee);
                }
                break;
                case PAY_BORROW: {
                    String payee = getPersonDisplayNameOrDefault(personPayee,unknown);
                    String payer = getAccountNameOrDefault(accountPayer,unknown);
                    return getContext().getString(R.string.text_transaction_history_description_pay_borrow,payee,payer);
                }
            }*/
            return null;
        }

        CharSequence getAmountText(@NonNull TransactionHistoryModel item) {
            Currency amount = item.getAmount();
            TransactionType type = item.getType();
            return amount.toString();
        }

        Drawable getPrimaryLogo(@NonNull TransactionHistoryModel item) {
            AccountModel account = item.getPayeeAccount();
            PersonModel person = item.getPayeePerson();
            if (null != account) {
                String text = account.getName().substring(0,1);
                int color = ColorGenerator.MATERIAL.getColor(account);
                return TextDrawable.builder().buildRound(text,color);
            }
            else if (null != person) {
                String text = getPersonDrawableText(person);
                int color = ColorGenerator.MATERIAL.getColor(person);
                return TextDrawable.builder().buildRound(text,color);
            }
            return null;
        }

        Drawable getSecondaryLogo(@NonNull TransactionHistoryModel item) {
            return null;
        }

        @NonNull
        String getAccountNameOrDefault(@Nullable AccountModel account, @NonNull String defaultValue) {
            if (null == account) {
                return defaultValue;
            }
            return account.getName();
        }

        @NonNull
        String getPersonDisplayNameOrDefault(@Nullable PersonModel person, @NonNull String defaultValue) {
            if (null == person) {
                return defaultValue;
            }
            return getPersonDisplayName(person);
        }

        @NonNull
        String getPersonDisplayName(@NonNull PersonModel person) {
            String firstName = person.getFirstName();
            String lastName = person.getLastName();
            if (null == lastName) {
                return firstName;
            }
            else {
                return firstName+" "+lastName;
            }
        }

        @NonNull
        String getPersonDrawableText(@NonNull PersonModel person) {
            String firstName = person.getFirstName();
            String lastName = person.getLastName();
            if (null == lastName) {
                return firstName.substring(0,1);
            }
            else {
                return String.valueOf(firstName.charAt(0)+lastName.charAt(0));
            }
        }
    }
}
