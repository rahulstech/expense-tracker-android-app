package dreammaker.android.expensetracker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.databinding.LayoutAccountListItemBinding;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.text.SpannableStringUtil;
import dreammaker.android.expensetracker.text.Spans;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ResourceUtil;

@SuppressWarnings("unused")
public class AccountsChooserAdapter
        extends BaseOnlySectionItemCheckableAdapter<Integer, AccountModel, AccountsChooserAdapter.SectionHeaderViewHolder, AccountsChooserAdapter.SectionItemViewHolder> {

    private static final ItemCallback CALLBACK = new ItemCallback() {
        @Override
        protected boolean areItemsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            if (type == SECTION_ITEM_TYPE) {
                return Objects.equals(((AccountModel) oldData).getId(), ((AccountModel) newData).getId());
            }
            return oldData.hashCode() == newData.hashCode();
        }

        @Override
        protected boolean areContentsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return Objects.equals(oldData,newData);
        }
    };

    public static final int HEADER_FREQUENTLY_USED = 1;

    public static final int HEADER_OTHERS = 2;

    private final List<Integer> mHeaders = Arrays.asList(HEADER_FREQUENTLY_USED,HEADER_OTHERS);

    private final int mHighlightColor;

    private String mQuery;

    public AccountsChooserAdapter(@NonNull Context context) {
        super(context, CALLBACK);
        mHighlightColor = ResourceUtil.getThemeColor(context,R.attr.colorSecondary);
    }

    @Override
    public void submitList(@Nullable List<AccountModel> list) {
        throw new RuntimeException("use filter(List,String) instead");
    }

    public void filter(@Nullable List<AccountModel> list, String query) {
        mQuery = query;
        if (null == list || list.isEmpty()) {
            performSubmit(Collections.emptyList(),Collections.emptyList(),Collections.emptyList());
        }
        else {
            super.submitList(list);
        }
    }

    public String getQuery() {
        return mQuery;
    }

    private int getHighlightColor() {
        return mHighlightColor;
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == SECTION_ITEM_TYPE) {
            return ((AccountModel) getData(position)).getId();
        }
        return RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    protected AsyncSectionBuilder<Integer, AccountModel> onCreateSectionBuilder(@Nullable List<AccountModel> list) {
        AsyncItemBuilder builder = new AsyncItemBuilder(list,mHeaders);
        builder.setQuery(getQuery());
        return builder;
    }

    @NonNull
    @Override
    protected SectionHeaderViewHolder onCreateSectionHeaderViewHolder(@NonNull ViewGroup parent, int type) {
        View view = getLayoutInflater().inflate(R.layout.layout_simple_list_item_1,parent,false);
        SectionHeaderViewHolder holder = new SectionHeaderViewHolder(view);
        holder.setAdapter(this);
        return holder;
    }

    @NonNull
    @Override
    protected SectionItemViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutAccountListItemBinding binding = LayoutAccountListItemBinding.inflate(getLayoutInflater(),parent,false);
        SectionItemViewHolder holder = new SectionItemViewHolder(binding);
        holder.setAdapter(this);
        return holder;
    }

    @Override
    protected void onBindSectionHeaderViewHolder(@NonNull SectionHeaderViewHolder holder, int adapterPosition) {
        holder.bind(getData(adapterPosition));
    }

    @Override
    protected void onBindSectionItemViewHolder(@NonNull SectionItemViewHolder holder, int adapterPosition) {
        holder.bind(getData(adapterPosition));
        holder.setChecked(getChoiceModel().isChecked(adapterPosition));
    }

    @NonNull
    @Override
    protected Object getChoiceKeyFromData(AccountModel data) {
        return data.getId();
    }

    public static class SectionHeaderViewHolder extends BaseViewHolder<Integer> {

        private final TextView text1;

        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = findViewById(R.id.text1);
        }

        @Override
        protected void onBindNonNull(@NonNull Integer item) {
            if (item == HEADER_FREQUENTLY_USED) {
                text1.setText(R.string.label_frequently_used);
            }
            else {
                text1.setText(R.string.label_others);
            }
        }
    }

    public class SectionItemViewHolder extends BaseViewHolder<AccountModel> {

        private final LayoutAccountListItemBinding mBinding;

        public SectionItemViewHolder(LayoutAccountListItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @Override
        protected void onBindNonNull(@NonNull AccountModel item) {
            String name = item.getName();
            Drawable logo = getDefaultAccountLogo(item);
            mBinding.logo.setImageDrawable(logo);
            mBinding.name.setText(highlight(name,getQuery()));
            mBinding.balance.setText(item.getBalance().toString());
        }

        public void setChecked(boolean checked) {

        }

        private Drawable getDefaultAccountLogo(AccountModel account) {
            String name = account.getName();
            return DrawableUtil.getAccountDefaultLogo(name);
        }

        private CharSequence highlight(CharSequence text, CharSequence phrase) {
            if (TextUtils.isEmpty(phrase)) {
                return text;
            }
            int start = TextUtil.indexOfIgnoreCase(text,phrase);
            int end = start+phrase.length();
            int color = getHighlightColor();
            Object span = Spans.textColor(color);
            return new SpannableStringUtil()
                    .append(text,span,start,end)
                    .toSpannableString();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncItemBuilder extends AsyncSectionBuilder<Integer,AccountModel> {

        private List<AccountModel> mFrequentlyUsed;

        private String mQuery;

        public AsyncItemBuilder(@Nullable List<AccountModel> items, @Nullable List<Integer> headers) {
            super(items, headers);
        }

        public void setQuery(String query) {
            this.mQuery = query;
        }

        @NonNull
        @Override
        protected List<AccountModel> onBeforeBuildSections(@NonNull List<AccountModel> items) {
            final String query = this.mQuery;
            List<AccountModel> accounts;
            if (TextUtils.isEmpty(query)) {
                accounts = items;
            }
            else {
                accounts = filter(items,query);
            }
            sort(accounts);
            return accounts;
        }

        private List<AccountModel> filter(List<AccountModel> accounts, String query) {
            ArrayList<AccountModel> list = new ArrayList<>();
            for (AccountModel ac : accounts) {
                String name = ac.getName();
                if (TextUtil.containsIgnoreCase(name,query)) {
                    list.add(ac);
                }
            }
            return list;
        }

        private void sort(List<AccountModel> items) {
            if (items.isEmpty()) {
                return;
            }
            items.sort(Comparator.comparingInt(AccountModel::getUsageCount).reversed());
            if (items.size() > Constants.FREQUENTLY_USED_DISPLAY_COUNT) {
                ArrayList<AccountModel> frequent = new ArrayList<>();
                for (int i=0; i<Constants.FREQUENTLY_USED_DISPLAY_COUNT; i++) {
                    AccountModel account = items.get(i);
                    if (account.getUsageCount() > 0) {
                        frequent.add(items.get(i));
                    }
                }
                if (!frequent.isEmpty()) {
                    mFrequentlyUsed = frequent;
                }
            }
        }

        @NonNull
        @Override
        protected Integer onCreateSectionHeader(@NonNull AccountModel item) {
            if (null != mFrequentlyUsed && mFrequentlyUsed.contains(item)) {
                return HEADER_FREQUENTLY_USED;
            }
            return HEADER_OTHERS;
        }

        @Override
        protected boolean belongsToSection(@NonNull AccountModel item, @NonNull Integer header) {
            return header.equals(onCreateSectionHeader(item));
        }

        @Override
        protected void onAfterBuildSections(@NonNull List<AccountModel> items, @NonNull List<Integer> headers, @NonNull List<ListItem> listItems) {
            SparseArrayCompat<Object> map = prepareChoiceKeyMap(listItems);
            if (!isCancelled()) {
                postChoiceKeyMap(map);
            }
        }
    }
}
