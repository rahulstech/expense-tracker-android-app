package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.databinding.LayoutAccountListItemBinding;
import dreammaker.android.expensetracker.text.TextUtil;

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
            return Objects.equals(oldData,newData);
        }

        @Override
        protected boolean areContentsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return Objects.equals(oldData,newData);
        }
    };

    private String mQuery = null;

    public AccountsAdapter(@NonNull Context context) {
        super(context, CALLBACK);
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

    @NonNull
    @Override
    protected AsyncSectionBuilder<String, AccountModel> onCreateSectionBuilder(@Nullable List<AccountModel> list) {
        AsyncItemBuilder builder = new AsyncItemBuilder(list);
        builder.setQuery(mQuery);
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

    public static class SectionItemViewHolder extends BaseViewHolder<AccountModel> {

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
            mBinding.name.setText(name);
            mBinding.balance.setText(item.getBalance().toString());
        }

        private Drawable getDefaultAccountLogo(AccountModel account) {
            String name = account.getName();
            int color = ColorGenerator.MATERIAL.getColor(name);
            String text;
            if (TextUtils.isEmpty(name)) {
                text = "";
            }
            else {
                text = name.substring(0,1);
            }
            return TextDrawable.builder()
                    .beginConfig().toUpperCase().endConfig()
                    .buildRound(text,color);
        }
    }

    private static class AsyncItemBuilder extends AsyncSectionBuilder<String,AccountModel> {

        private String mQuery;

        public AsyncItemBuilder(@Nullable List<AccountModel> items) {
            super(items);
        }

        public void setQuery(String query) {
            mQuery = query;
        }

        @NonNull
        @Override
        protected List<AccountModel> onBeforeBuildSections(@NonNull List<AccountModel> items) {
            List<AccountModel> accounts;
            if (TextUtils.isEmpty(mQuery)) {
                accounts = items;
            }
            else {
                accounts = filter(items,mQuery);
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
            accounts.sort((left,right)->{
                String nameLeft = left.getName();
                String nameRight = right.getName();
                String firstLeft = TextUtil.getDisplayLabel(nameLeft);
                String firstRight = TextUtil.getDisplayLabel(nameRight);
                if (TextUtil.isNonLetter(firstLeft) && TextUtil.isNonLetter(firstRight)) {
                    return 0;
                }
                else if (TextUtil.isNonLetter(firstLeft) || TextUtil.isNonLetter(firstRight)) {
                    // TODO: implement proper sorting for non letter
                    return Integer.MAX_VALUE;
                }
                return nameLeft.compareToIgnoreCase(nameRight);
            });
        }

        @NonNull
        @Override
        protected String onCreateSectionHeader(@NonNull AccountModel item) {
            return TextUtil.getDisplayLabel(item.getName());
        }

        @Override
        protected boolean belongsToSection(@NonNull AccountModel item, @NonNull String header) {
            String expected = onCreateSectionHeader(item);
            return expected.equals(header);
        }
    }
}
