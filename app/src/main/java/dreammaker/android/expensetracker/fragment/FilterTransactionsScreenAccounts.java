package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.view.AbsSelectionListAdapter;
import dreammaker.android.expensetracker.view.AccountsSelectionAdapter;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

public class FilterTransactionsScreenAccounts extends BaseFragment<FilterTransactionsScreenAccounts.FilterTransactionsScreenSearchableListContentViewHolder>
        implements AbsSelectionListAdapter.OnItemSelectionChangeListener {

    private static final String TAG = "FilterTransactionScreenSearchableContent";

    private TransactionsViewModel viewModel;
    private AccountsSelectionAdapter adapter;

    @Override
    public void onItemSelectionChange(int position, boolean checked) {
        final Account item = adapter.getItem(position);
        if (checked) {
            viewModel.getWorkingFilterParams().addSelectedAccount(item);
        }
        else {
            viewModel.getWorkingFilterParams().removeSelectedAccount(item);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setFragmentTitle(R.string.label_filter_transaction);
        if (Check.isNonNull(getActivity())) {
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(TransactionsViewModel.class);
        }
    }

    @NonNull
    @Override
    protected FilterTransactionsScreenSearchableListContentViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        FilterTransactionsScreenSearchableListContentViewHolder vh = new FilterTransactionsScreenSearchableListContentViewHolder(
                inflater.inflate(R.layout.screen_searchable_list_content, container, false));

        return vh;
    }

    @Override
    protected void onFragmentViewHolderCreated(@NonNull FilterTransactionsScreenSearchableListContentViewHolder vh) {
        adapter = new AccountsSelectionAdapter(getContext());
        adapter.setOnItemSelectionChangeListener(this);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() { vh.configEmptyList(adapter.isEmpty()); }

            @Override
            public void onInvalidated() { vh.configEmptyList(adapter.isEmpty()); }
        });
        vh.empty.setText(R.string.no_account);
        vh.list.setAdapter(adapter);
        vh.search.setQueryHint(getString(R.string.search_account));
        vh.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (Check.isEmptyString(newText)){
                    adapter.getFilter().filter(null);
                    return true;
                }
                return false;
            }
        });
        viewModel.getAllAccountsNameAndId().observe(this, this::onAccountsLoaded);
    }

    private void onAccountsLoaded(List<Account> accounts) {
        adapter.changeList(accounts);
        adapter.setCheckedItems(viewModel.getWorkingFilterParams().getSelectedAccounts());
    }

    static class FilterTransactionsScreenSearchableListContentViewHolder extends BaseFragment.FragmentViewHolder{
        SearchView search;
        ListView list;
        TextView empty;

        FilterTransactionsScreenSearchableListContentViewHolder(@NonNull View root) {
            super(root);
            search = findViewById(R.id.search);
            list = findViewById(R.id.list);
            empty = findViewById(R.id.empty);
        }

        private void configEmptyList( boolean isEmpty){
            if (isEmpty){
                list.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
            }
            else {
                empty.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
            }
        }
    }
}
