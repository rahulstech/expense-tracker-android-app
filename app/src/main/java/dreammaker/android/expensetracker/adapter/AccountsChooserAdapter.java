package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.listener.ChoiceModel;

public class AccountsChooserAdapter
        extends SectionedListAdapter<String, AccountModel, AccountsChooserAdapter.SectionHeaderViewHolder, AccountsChooserAdapter.SectionItemViewHolder>
        implements ChoiceModel.Callback {

    private static final ItemCallback CALLBACK = new ItemCallback() {
        @Override
        protected boolean areItemsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return false;
        }

        @Override
        protected boolean areContentsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return false;
        }
    };

    private final List<String> mHeaders;

    private String mQuery;

    private ChoiceModel mChoiceModel;

    public AccountsChooserAdapter(@NonNull Context context) {
        super(context, CALLBACK);
        mHeaders = Arrays.asList(

        );
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

    public void setChoiceModel(ChoiceModel model) {
        mChoiceModel = model;
    }

    public ChoiceModel getChoiceModel() {
        return mChoiceModel;
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
    protected AsyncSectionBuilder<String, AccountModel> onCreateSectionBuilder(@Nullable List<AccountModel> list) {
        return new AsyncItemBuilder(list,mHeaders);
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
        holder.setChecked(getChoiceModel().isChecked(adapterPosition));
    }

    @NonNull
    @Override
    public Object getKey(int position) {
        return null;
    }

    @Override
    public int getPosition(@NonNull Object key) {
        return 0;
    }

    @Override
    public boolean isCheckable(int position) {
        return false;
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

        @Override
        protected void onBindNonNull(@NonNull AccountModel item) {

        }

        public void setChecked(boolean checked) {

        }
    }

    private class AsyncItemBuilder extends AsyncSectionBuilder<String,AccountModel> {

        public AsyncItemBuilder(@Nullable List<AccountModel> items, @Nullable List<String> headers) {
            super(items, headers);
        }

        @NonNull
        @Override
        protected List<AccountModel> onBeforeBuildSections(@NonNull List<AccountModel> items) {
            final String query = mQuery;
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

        private List<AccountModel> filter(List<AccountModel> items, String query) {
            // TODO: filter accounts by query
            return items;
        }

        private void sort(List<AccountModel> items) {
            // TODO: sort accounts
        }

        @NonNull
        @Override
        protected String onCreateSectionHeader(@NonNull AccountModel item) {
            return null;
        }

        @Override
        protected boolean belongsToSection(@NonNull AccountModel item, @NonNull String header) {
            String expected = onCreateSectionHeader(item);
            return expected.equals(header);
        }
    }
}
