package dreammaker.android.expensetracker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.LayoutTransactionHistoryItemBinding;
import dreammaker.android.expensetracker.drawable.CheckableDrawableWrapper;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.fragment.parcelable.HistoryFilterData;
import dreammaker.android.expensetracker.text.TextUtil;

@SuppressWarnings("unused")
public class SectionedTransactionHistoryAdapter
        extends BaseOnlySectionItemCheckableAdapter<SectionedTransactionHistoryAdapter.HeaderData, TransactionHistoryModel,
        SectionedTransactionHistoryAdapter.HeaderViewHolder, SectionedTransactionHistoryAdapter.ChildViewHolder> {

    private static final String TAG = SectionedTransactionHistoryAdapter.class.getSimpleName();

    private static final ItemCallback CALLBACK = new ItemCallback() {
        @Override
        protected boolean areItemsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            if (type == SECTION_ITEM_TYPE) {
                return Objects.equals(((TransactionHistoryModel) oldData).getId(),((TransactionHistoryModel) newData).getId());
            }
            return oldData.hashCode() == newData.hashCode();
        }

        @Override
        protected boolean areContentsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return Objects.equals(oldData,newData);
        }
    };

    public static final int HEADER_DATE = 1;

    public static final int HEADER_MONTH = 2;

    private int mHeaderType = HEADER_DATE;

    private HistoryFilterData mQuery;

    public SectionedTransactionHistoryAdapter(@NonNull Context context) {
        super(context, CALLBACK);
    }

    public void setHeaderType(int type) {
        mHeaderType = type;
    }

    public int getHeaderType() {
        return mHeaderType;
    }

    public void filter(@Nullable List<TransactionHistoryModel> items, @Nullable HistoryFilterData query) {
        mQuery = query;
        super.submitList(items);
    }

    @Override
    public void submitList(@Nullable List<TransactionHistoryModel> list) {
        throw new RuntimeException("use filter(List,HistoryFilterData) instead");
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == SECTION_ITEM_TYPE) {
            return ((TransactionHistoryModel) getData(position)).getId();
        }
        return RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    protected AsyncSectionBuilder<HeaderData, TransactionHistoryModel> onCreateSectionBuilder(@Nullable List<TransactionHistoryModel> list) {
        AsyncItemBuilder builder = new AsyncItemBuilder(list,mHeaderType);
        builder.setQuery(mQuery);
        return builder;
    }

    @NonNull
    @Override
    protected HeaderViewHolder onCreateSectionHeaderViewHolder(@NonNull ViewGroup parent, int type) {
        View view = getLayoutInflater().inflate(R.layout.layout_simple_list_item_1,parent,false);
        HeaderViewHolder holder = new HeaderViewHolder(view);
        holder.setAdapter(this);
        return holder;
    }

    @NonNull
    @Override
    protected ChildViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutTransactionHistoryItemBinding binding
                = LayoutTransactionHistoryItemBinding.inflate(getLayoutInflater(),parent,false);
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
        holder.setChecked(getChoiceModel().isChecked(adapterPosition));
    }

    @NonNull
    @Override
    protected Object getChoiceKeyFromData(TransactionHistoryModel data) {
        return data.getId();
    }

    public static class HeaderData {

        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy");

        private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

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
        public String toString(Resources res) {
            if (type == HEADER_DATE) {
                LocalDate date = (LocalDate) data;
                if (LocalDate.now().isEqual(date)) {
                    return res.getString(R.string.today);
                }
                else if (LocalDate.now().minusDays(1).isEqual(date)) {
                    return res.getString(R.string.yesterday);
                }
                else {
                    return DATE_FORMAT.format(date);
                }
            }
            else {
                LocalDate month = (LocalDate) data;
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

        private final TextView text1;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            text1 = findViewById(R.id.text1);
        }

        @Override
        protected void onBindNonNull(@NonNull HeaderData item) {
            text1.setText(item.toString(getContext().getResources()));
        }
    }

    public static class ChildViewHolder extends BaseViewHolder<TransactionHistoryModel> {

        final LayoutTransactionHistoryItemBinding mBinding;

        private CheckableDrawableWrapper mWrapper;

        public ChildViewHolder(@NonNull LayoutTransactionHistoryItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void setChecked(boolean checked) {
            mWrapper.setChecked(checked);
        }

        @Override
        protected void onBindNull() {}

        @Override
        protected void onBindNonNull(@NonNull TransactionHistoryModel item) {
            TransactionType type = item.getType();
            mBinding.description.setText(getDescription(item));
            mBinding.amount.setText(item.getAmount().toString());
            if (type == TransactionType.INCOME) {
                Drawable drawable = getPayeeDefaultDrawable(item);
                mWrapper = new CheckableDrawableWrapper(getContext(),drawable);
                mBinding.logoPrimary.setImageDrawable(mWrapper);
                mBinding.labelSecondary.setText(null);
                mBinding.logoSecondary.setImageDrawable(null);
            }
            else if (type == TransactionType.EXPENSE) {
                Drawable drawable = getPayerDefaultDrawable(item);
                mWrapper = new CheckableDrawableWrapper(getContext(),drawable);
                mBinding.logoPrimary.setImageDrawable(mWrapper);
                mBinding.labelSecondary.setText(null);
                mBinding.logoSecondary.setImageDrawable(null);
            }
            else {
                Drawable drawable = getPayeeDefaultDrawable(item);
                mWrapper = new CheckableDrawableWrapper(getContext(),drawable);
                mBinding.logoPrimary.setImageDrawable(mWrapper);
                mBinding.logoSecondary.setImageDrawable(getPayerDefaultDrawable(item));
                switch (type) {
                    case DUE:
                    case PAY_BORROW: {
                        mBinding.labelSecondary.setText(getContext().getText(R.string.label_paid_from));
                    }
                    case BORROW:
                    case PAY_DUE: {
                        mBinding.labelSecondary.setText(getContext().getString(R.string.label_paid_by));
                    }
                    case MONEY_TRANSFER:
                    case DUE_TRANSFER:
                    case BORROW_TO_DUE_TRANSFER:
                    case BORROW_TRANSFER:{
                        mBinding.labelSecondary.setText(getContext().getString(R.string.label_transferred_from));
                    }
                }
            }
        }

        Drawable getPayeeDefaultDrawable(TransactionHistoryModel item) {
            if (null != item.getPayeeAccount()) {
                return DrawableUtil.getAccountDefaultLogo(item.getPayeeAccount().getName());
            }
            else if (null != item.getPayeePerson()) {
                return getPersonDefaultPhoto(item.getPayeePerson());
            }
            return DrawableUtil.getDrawableUnknown();
        }

        Drawable getPayerDefaultDrawable(TransactionHistoryModel item) {
            if (null != item.getPayerAccount()) {
                return DrawableUtil.getAccountDefaultLogo(item.getPayerAccount().getName());
            }
            else if (null != item.getPayerPerson()) {
                return getPersonDefaultPhoto(item.getPayerPerson());
            }
            return DrawableUtil.getDrawableUnknown();
        }

        Drawable getPersonDefaultPhoto(PersonModel person) {
            final String firstName = person.getFirstName();
            final String lastName = person.getLastName();
            return DrawableUtil.getPersonDefaultPhoto(firstName,lastName,true);
        }

        CharSequence getDescription(TransactionHistoryModel item) {
            String description = item.getDescription();
            TransactionType type = item.getType();
            String payee, payer;
            if (item.getPayeeAccount() != null) {
                payee = item.getPayeeAccount().getName();
            }
            else if (item.getPayeePerson() != null) {
                PersonModel person = item.getPayeePerson();
                payee = TextUtil.getDisplayNameForPerson(person.getFirstName(),person.getLastName(),true,
                        getContext().getString(R.string.label_unknown));
            }
            else {
                payee = null;
            }
            if (item.getPayerAccount() != null) {
                payer = item.getPayerAccount().getName();
            }
            else if (item.getPayerPerson() != null) {
                PersonModel person = item.getPayerPerson();
                payer = TextUtil.getDisplayNameForPerson(person.getFirstName(),person.getLastName(),true,
                        getContext().getString(R.string.label_unknown));
            }
            else {
                payer = null;
            }
            String text = TextUtil.getTransactionHistoryDescription(getContext().getResources(),type,payer,payee,description);
            if (null == text) {
                return null;
            }
            return text.replaceAll("\\s+"," ");
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncItemBuilder extends AsyncSectionBuilder<HeaderData,TransactionHistoryModel> {

        private final int mHeaderType;

        private HistoryFilterData mQuery;

        public AsyncItemBuilder(@Nullable List<TransactionHistoryModel> items, int headerType) {
            super(items);
            mHeaderType = headerType;
        }

        public void setQuery(HistoryFilterData query) {
            mQuery = query;
        }

        @NonNull
        @Override
        protected List<TransactionHistoryModel> onBeforeBuildSections(@NonNull List<TransactionHistoryModel> items) {
            final HistoryFilterData query = mQuery;
            List<TransactionHistoryModel> histories;
            if (null == query) {
                histories = items;
            }
            else {
                histories = filter(items,query);
            }
            return histories;
        }

        private List<TransactionHistoryModel> filter(List<TransactionHistoryModel> histories, HistoryFilterData query) {
            ArrayList<TransactionHistoryModel> filtered = new ArrayList<>();
            for (TransactionHistoryModel history : histories) {
                if (match(history,query)) {
                    filtered.add(history);
                }
            }
            return filtered;
        }

        private boolean match(TransactionHistoryModel history, HistoryFilterData query) {
            final LocalDate rangeStart = query.getRangeStart();
            final LocalDate rangeEnd = query.getRangeEnd();
            final EnumSet<TransactionType> types = query.getTypes();
            final List<Long> accountIds = query.getAccountIds();
            final List<Long> personIds = query.getPersonIds();
            final LocalDate date = history.getWhen();

            if (null != rangeStart && date.compareTo(rangeStart) < 0) {
                return false;
            }
            if (null != rangeEnd && date.compareTo(rangeEnd) > 0) {
                return false;
            }
            if (null != types && !types.contains(history.getType())) {
                return false;
            }
            if (null != accountIds) {
                Long payee = history.getPayeeAccountId();
                Long payer = history.getPayerAccountId();
                if (null == payee && null == payer) {
                    return false;
                }
                if (null != payee && !accountIds.contains(payee)) {
                    return false;
                }
                if (null != payer && !accountIds.contains(payer)) {
                    return false;
                }
            }
            if (null != personIds) {
                Long payee = history.getPayeePersonId();
                Long payer = history.getPayerPersonId();
                if (null == payee && null == payer) {
                    return false;
                }
                if (null != payee && !personIds.contains(payee)) {
                    return false;
                }
                if (null != payer && !personIds.contains(payer)) {
                    return false;
                }
            }
            return true;
        }

        @NonNull
        @Override
        protected HeaderData onCreateSectionHeader(@NonNull TransactionHistoryModel item) {
            LocalDate data = LocalDate.from(item.getWhen());
            if (mHeaderType == HEADER_DATE) {
                return new HeaderData(HEADER_DATE,data);
            }
            else {
                return new HeaderData(HEADER_MONTH,data);
            }
        }

        @Override
        protected boolean belongsToSection(@NonNull TransactionHistoryModel item, @NonNull HeaderData header) {
            LocalDate when = item.getWhen();
            if (mHeaderType == HEADER_DATE) {
                return when.isEqual(header.getData());
            }
            else {
                LocalDate month = header.getData();
                return when.getMonthValue() == month.getMonthValue()
                        && when.getYear() == month.getYear();
            }
        }

        @Override
        protected void onAfterBuildSections(@NonNull List<TransactionHistoryModel> items, @NonNull List<HeaderData> headers,
                                            @NonNull List<ListItem> listItems) {
            SparseArrayCompat<Object> map = prepareChoiceKeyMap(listItems);
            if (!isCancelled()) {
                postChoiceKeyMap(map);
            }
        }
    }
}
