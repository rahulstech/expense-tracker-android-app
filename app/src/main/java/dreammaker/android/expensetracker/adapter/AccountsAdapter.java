package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.database.model.AccountModel;
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

    @Nullable
    private String mQuery = null;

    public AccountsAdapter(@NonNull Context context) {
        super(context, CALLBACK);
        setHasListFooter(true);
    }

    public void filter(@Nullable List<AccountModel> accounts, @Nullable String query) {
        mQuery = query;
        submitList(accounts);
    }

    @Nullable
    public String getQuery() {
        return mQuery;
    }

    @Override
    public boolean hasListFooter() {
        return super.hasListFooter() && getItemCount() > 0;
    }

    @NonNull
    @Override
    protected AsyncSectionBuilder<String, AccountModel> onCreateSectionBuilder(@Nullable List<AccountModel> list) {
        return new AsyncItemBuilder(list,mQuery);
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

    }

    @Override
    protected void onBindSectionItemViewHolder(@NonNull SectionItemViewHolder holder, int adapterPosition) {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateListFooterViewHolder(@NonNull ViewGroup parent) {
        // TODO: implement method
        return BaseViewHolder.create(getContext(),parent,0);
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

    public static class AsyncItemBuilder extends AsyncSectionBuilder<String,AccountModel> {

        public static final String HEADER_NON_LETTER = "#";

        private final String mQuery;

        public AsyncItemBuilder(@Nullable List<AccountModel> items, @Nullable String query) {
            super(items);
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

        @NonNull
        private List<AccountModel> filter(@NonNull List<AccountModel> accounts, String query) {
            ArrayList<AccountModel> list = new ArrayList<>();
            for (AccountModel ac : accounts) {
                String name = ac.getName();
                if (name.contains(query)) {
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
