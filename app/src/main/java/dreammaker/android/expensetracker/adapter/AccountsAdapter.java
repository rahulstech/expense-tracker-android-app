package dreammaker.android.expensetracker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.databinding.LayoutAccountListItemBinding;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.text.SpannableStringUtil;
import dreammaker.android.expensetracker.text.Spans;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.ResourceUtil;

@SuppressWarnings("unused")
public class AccountsAdapter
        extends SectionedListAdapter<String, AccountModel, AccountsAdapter.SectionHeaderViewHolder, AccountsAdapter.SectionItemViewHolder> {

    private static final String TAG = "AccountsAdapter";

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

    private final int mHighlightColor;

    private String mQuery = null;

    public AccountsAdapter(@NonNull Context context) {
        super(context, CALLBACK);
        mHighlightColor = ResourceUtil.getThemeColor(context,R.attr.colorSecondary);
    }

    @Override
    public void submitList(@Nullable List<AccountModel> list) {
        throw new RuntimeException("use filter(List,String) instead");
    }

    public void filter(@Nullable List<AccountModel> accounts, String query) {
        mQuery = query;
        if (null == accounts || accounts.isEmpty()) {
            performSubmit(Collections.emptyList(),Collections.emptyList(),Collections.emptyList());
        }
        else {
            super.submitList(accounts);
        }
    }

    public String getQuery() {
        return mQuery;
    }

    private int getHighlightColor() {
        return mHighlightColor;
    }

    @NonNull
    @Override
    protected AsyncSectionBuilder<String, AccountModel> onCreateSectionBuilder(@Nullable List<AccountModel> list) {
        AsyncItemBuilder builder = new AsyncItemBuilder(list);
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
    }

    public static class SectionHeaderViewHolder extends BaseViewHolder<String> {

        private final TextView text1;

        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = findViewById(R.id.text1);
        }

        @Override
        protected void onBindNonNull(@NonNull String item) {
            text1.setText(item);
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

    private static class AsyncItemBuilder extends AsyncSectionBuilder<String,AccountModel> {

        private String mQuery;

        public AsyncItemBuilder(@Nullable List<AccountModel> items) {
            super(items);
        }

        public void setQuery(String query) {
            this.mQuery = query;
        }

        @NonNull
        @Override
        protected List<AccountModel> onBeforeBuildSections(@NonNull List<AccountModel> items) {
            String query = this.mQuery;
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

        private List<AccountModel> filter(@NonNull List<AccountModel> accounts, String query) {
            ArrayList<AccountModel> list = new ArrayList<>();
            for (AccountModel ac : accounts) {
                String name = ac.getName();
                if (TextUtil.containsIgnoreCase(name,query)) {
                    list.add(ac);
                }
            }
            return list;
        }

        private void sort(List<AccountModel> accounts) {
            // extract the accounts with name stating with non letters
            ArrayList<AccountModel> sublist = new ArrayList<>();
            for (AccountModel account : accounts) {
                String name = account.getName();
                int code0 = name.codePointAt(0);
                if (!TextUtil.isLetter(code0)) {
                    sublist.add(account);
                }
            }
            // remove the extracted sublist
            accounts.removeAll(sublist);
            // sort remaining list by name
            accounts.sort((left,right)->{
                String nameLeft = left.getName();
                String nameRight = right.getName();
                return nameLeft.compareToIgnoreCase(nameRight);
            });
            // sort the extracted sublist by name
            sublist.sort((left,right)->{
                String nameLeft = left.getName();
                String nameRight = right.getName();
                return nameLeft.compareToIgnoreCase(nameRight);
            });
            // append the sublist
            accounts.addAll(sublist);
        }

        @NonNull
        @Override
        protected String onCreateSectionHeader(@NonNull AccountModel item) {
            return TextUtil.getDisplayLabelLetterOnly(item.getName(),TextUtil.DEFAULT_DISPLAY_LABEL_NON_LETTER);
        }

        @Override
        protected boolean belongsToSection(@NonNull AccountModel item, @NonNull String header) {
            String expected = onCreateSectionHeader(item);
            return expected.equals(header);
        }
    }
}
