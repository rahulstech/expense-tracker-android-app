package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseLongArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.listener.ChoiceModel;
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
        return null;
    }

    @NonNull
    @Override
    protected SectionItemViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup parent, int type) {
        return null;
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

        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class SectionItemViewHolder extends BaseViewHolder<AccountModel> {

        public SectionItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private static class AsyncItemBuilder extends AsyncSectionBuilder<String,AccountModel> {

        public static final String HEADER_NON_LETTER = "#";

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
            accounts.sort((left,right)->{
                String nameLeft = left.getName();
                String nameRight = right.getName();
                String firstLeft = TextUtil.getDisplayLabel(nameLeft);
                String firstRight = TextUtil.getDisplayLabel(nameRight);
                if (TextUtil.isNonLetter(firstLeft) || TextUtil.isNonLetter(firstRight)) {
                    // TODO: implement proper sorting for non letter
                    return Integer.MIN_VALUE;
                }
                return nameLeft.compareToIgnoreCase(nameRight);
            });
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
