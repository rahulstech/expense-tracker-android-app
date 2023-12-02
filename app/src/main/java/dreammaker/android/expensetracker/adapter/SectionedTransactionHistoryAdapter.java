package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.LayoutTransactionHistoryItemChildBinding;
import dreammaker.android.expensetracker.databinding.LayoutTransactionHistoryItemHeaderBinding;
import dreammaker.android.expensetracker.text.TextUtil;

@SuppressWarnings("unused")
public class SectionedTransactionHistoryAdapter
        extends SectionedListAdapter<SectionedTransactionHistoryAdapter.HeaderData, TransactionHistoryModel,
        SectionedTransactionHistoryAdapter.HeaderViewHolder, SectionedTransactionHistoryAdapter.ChildViewHolder> {

    private static final String TAG = SectionedTransactionHistoryAdapter.class.getSimpleName();

    private static final ItemCallback CALLBACK = new ItemCallback() {
        @Override
        protected boolean areItemsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            if (type == SECTION_ITEM_TYPE) {
                return Objects.equals(((TransactionHistoryModel) oldData).getId(),((TransactionHistoryModel) newData).getId());
            }
            return Objects.equals(oldData,newData);
        }

        @Override
        protected boolean areContentsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return Objects.equals(oldData,newData);
        }
    };

    public static final int HEADER_DATE = 1;

    public static final int HEADER_MONTH = 2;

    private int mHeaderType = HEADER_DATE;

    public SectionedTransactionHistoryAdapter(@NonNull Context context) {
        super(context, CALLBACK);
        setHasListFooter(true);
    }

    public void changeHeaderType(int type) {
        mHeaderType = type;
        submitList(new LinkedList<>(getSubmittedList()));
    }

    public int getHeaderType() {
        return mHeaderType;
    }

    @NonNull
    @Override
    protected AsyncSectionBuilder<HeaderData, TransactionHistoryModel> onCreateSectionBuilder(@Nullable List<TransactionHistoryModel> list) {
        return new AsyncItemBuilder(list,mHeaderType);
    }

    @NonNull
    @Override
    protected HeaderViewHolder onCreateSectionHeaderViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutTransactionHistoryItemHeaderBinding binding
                = LayoutTransactionHistoryItemHeaderBinding.inflate(getLayoutInflater(),parent,false);
        HeaderViewHolder holder = new HeaderViewHolder(binding);
        holder.setAdapter(this);
        return holder;
    }

    @NonNull
    @Override
    protected ChildViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutTransactionHistoryItemChildBinding binding
                = LayoutTransactionHistoryItemChildBinding.inflate(getLayoutInflater(),parent,false);
        ChildViewHolder holder = new ChildViewHolder(binding);
        holder.setAdapter(this);
        return holder;
    }

    @Override
    protected void onBindSectionHeaderViewHolder(@NonNull HeaderViewHolder holder, int adapterPosition) {
        HeaderData data = getData(adapterPosition);
        holder.bind(data);
    }

    @Override
    protected void onBindSectionItemViewHolder(@NonNull ChildViewHolder holder, int adapterPosition) {
        TransactionHistoryModel data = getData(adapterPosition);
        holder.bind(data);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateListFooterViewHolder(@NonNull ViewGroup parent) {
        return BaseViewHolder.create(getContext(),parent, R.layout.layout_list_footer);
    }

    public static class HeaderData {

        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy");

        private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMM yyyy");

        private final int type;

        @NonNull
        private final Object data;

        public HeaderData(int type, @NonNull Object data) {
            this.type = type;
            this.data = data;
        }

        public int getType() {
            return type;
        }

        @SuppressWarnings("unchecked")
        public <T> T getData() {
            return (T) data;
        }

        @NonNull
        @Override
        public String toString() {
            if (type == HEADER_DATE) {
                LocalDate date = (LocalDate) data;
                return DATE_FORMAT.format(date);
            }
            else {
                Month month = (Month) data;
                return MONTH_FORMAT.format(month);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HeaderData)) return false;
            HeaderData that = (HeaderData) o;
            return type == that.type && data.equals(that.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, data);
        }
    }

    public static class HeaderViewHolder extends BaseViewHolder<HeaderData> {

        final LayoutTransactionHistoryItemHeaderBinding mBinding;

        public HeaderViewHolder(@NonNull LayoutTransactionHistoryItemHeaderBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @Override
        protected void onBindNull() {}

        @Override
        protected void onBindNonNull(@NonNull HeaderData item) {
            mBinding.text1.setText(item.toString());
        }
    }

    public static class ChildViewHolder extends BaseViewHolder<TransactionHistoryModel> {

        final LayoutTransactionHistoryItemChildBinding mBinding;

        public ChildViewHolder(@NonNull LayoutTransactionHistoryItemChildBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @Override
        protected void onBindNull() {}

        @Override
        protected void onBindNonNull(@NonNull TransactionHistoryModel item) {
            TransactionType type = item.getType();
            mBinding.logoPrimary.setImageDrawable(getPayeeDefaultDrawable(item));
            mBinding.description.setText(getDescription(item));
            mBinding.amount.setText(item.getAmount().toString());
            if (type == TransactionType.INCOME) {
                mBinding.logoSecondary.setText(null);
                mBinding.logoSecondary.setCompoundDrawablesRelative(null, null, null, null);
            }
            else {
                mBinding.logoSecondary.setCompoundDrawablesRelative(null,null,getPayerDefaultDrawable(item),null);
                switch (type) {
                    case DUE:
                    case EXPENSE:
                    case PAY_BORROW: {
                        mBinding.logoSecondary.setText(getContext().getText(R.string.label_paid_from));
                    }
                    case BORROW:
                    case PAY_DUE: {
                        mBinding.logoSecondary.setText(getContext().getString(R.string.label_paid_by));
                    }
                    case MONEY_TRANSFER:
                    case DUE_TRANSFER:
                    case BORROW_TO_DUE_TRANSFER: {
                        mBinding.logoSecondary.setText(getContext().getString(R.string.label_transferred_from));
                    }
                }
            }
        }

        Drawable getPayeeDefaultDrawable(TransactionHistoryModel item) {
            if (null != item.getPayeeAccount()) {
                return getTextDrawable(item.getPayeeAccount().getName());
            }
            else if (null != item.getPayeePerson()) {
                return getPersonDefaultPhoto(item.getPayeePerson());
            }
            return null;
        }

        Drawable getPayerDefaultDrawable(TransactionHistoryModel item) {
            if (null != item.getPayeeAccount()) {
                return getTextDrawable(item.getPayeeAccount().getName());
            }
            else if (null != item.getPayeePerson()) {
                return getPersonDefaultPhoto(item.getPayeePerson());
            }
            return null;
        }

        Drawable getPersonDefaultPhoto(PersonModel person) {
            final String firstName = person.getFirstName();
            final String lastName = person.getLastName();
            final String displayName = TextUtil.getDisplayNameForPerson(firstName,lastName,TextUtil.FIRST_NAME_FIRST);
            return getTextDrawable(displayName);
        }

        Drawable getTextDrawable(String text) {
            String drawableText = TextUtil.getDisplayLabel(text);
            int drawableBackground = ColorGenerator.MATERIAL.getColor(text);
            return TextDrawable.builder()
                    .beginConfig().bold().endConfig()
                    .buildRound(drawableText,drawableBackground);
        }

        CharSequence getDescription(TransactionHistoryModel item) {
            String description = item.getDescription();
            if (!TextUtils.isEmpty(description)) {
                // TODO: make single line
                return description;
            }
            TransactionType type = item.getType();
            switch (type) {
                case EXPENSE: {
                    return getContext().getText(R.string.message_debit_without_amount);
                }
                case INCOME: {
                    return getContext().getString(R.string.message_credit_without_amount);
                }
                case DUE_TRANSFER:
                case BORROW_TRANSFER:
                case BORROW_TO_DUE_TRANSFER:{
                    PersonModel payee = item.getPayeePerson();
                    String dest = TextUtil.getDisplayNameForPerson(payee.getFirstName(),payee.getLastName(), TextUtil.FIRST_NAME_FIRST);
                    return getContext().getString(R.string.message_transferred_to_without_amount,dest);
                }
                case BORROW:
                case PAY_DUE:
                case MONEY_TRANSFER: {
                    String dest = item.getPayeeAccount().getName();
                    return getContext().getString(R.string.message_received_in_without_amount,dest);
                }
                case DUE:
                case PAY_BORROW: {
                    PersonModel payee = item.getPayeePerson();
                    String dest = TextUtil.getDisplayNameForPerson(payee.getFirstName(),payee.getLastName(), TextUtil.FIRST_NAME_FIRST);
                    return getContext().getString(R.string.message_paid_to_without_amount,dest);
                }
            }
            return null;
        }
    }

    private static class AsyncItemBuilder extends AsyncSectionBuilder<HeaderData,TransactionHistoryModel> {

        private final int mHeaderType;

        public AsyncItemBuilder(@Nullable List<TransactionHistoryModel> items, int headerType) {
            super(items);
            mHeaderType = headerType;
        }

        @NonNull
        @Override
        protected List<TransactionHistoryModel> onBeforeBuildSections(@NonNull List<TransactionHistoryModel> items) {
            return super.onBeforeBuildSections(items);
        }

        @NonNull
        @Override
        protected HeaderData onCreateSectionHeader(@NonNull TransactionHistoryModel item) {
            if (mHeaderType == HEADER_DATE) {
                LocalDate data = LocalDate.from(item.getWhen());
                return new HeaderData(HEADER_DATE,data);
            }
            else {
                Month data = Month.from(item.getWhen());
                return new HeaderData(HEADER_MONTH,data);
            }
        }

        @Override
        protected boolean belongsToSection(@NonNull TransactionHistoryModel item, @NonNull HeaderData header) {
            if (mHeaderType == HEADER_DATE) {
                return item.getWhen().isEqual(header.getData());
            }
            else {
                return item.getWhen().getMonth().equals(header.getData());
            }
        }
    }
}
