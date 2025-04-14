package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.AboutAccount;
import dreammaker.android.expensetracker.database.Account;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.view.AccountsAdapter;
import dreammaker.android.expensetracker.view.BaseRecyclerViewListAdapterFilterable;
import dreammaker.android.expensetracker.view.OnItemChildClickListener;
import dreammaker.android.expensetracker.viewmodel.AccountsViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

public class AccountsList extends BaseListFragment<BaseListFragment.ListFragmentViewHolder> implements OnItemChildClickListener<AccountsAdapter, AccountsAdapter.AccountViewHolder> {

    private static final boolean DEBUG = true;
    private static final String TAG = "AccountsList";
    private static final int ID_SAVED_DATA = 959;

    private AccountsAdapter adapter;
    private AccountsViewModel viewModel;
    private TransactionsViewModel transactionsViewModel;
    private AccountListSaveData saveData;
    private NavController navController;

    public AccountsList() { super(); }

    public NavController getNavController() {
        return navController;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setFragmentTitle(R.string.label_accounts);
        if (Check.isNonNull(getActivity())){
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(AccountsViewModel.class);
            transactionsViewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(TransactionsViewModel.class);
            saveData = viewModel.getSavedData(ID_SAVED_DATA, new AccountListSaveData());
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (isViewHolderCreated()) {
            saveData.queryAccount = getQuery();
            saveData.adapterSaveData = adapter.onSaveData();
            viewModel.putSavedData(ID_SAVED_DATA, saveData);
        }
    }

    @NonNull
    @Override
    protected ListFragmentViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        View v = inflater.inflate(R.layout.list_content, container, false);
        ListFragmentViewHolder vh = new ListFragmentViewHolder(v);
        adapter = new AccountsAdapter(getContext());
        adapter.setOnItemChildClickListener(this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                configureEmptyContent(adapter.isEmpty());
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {configureEmptyContent(adapter.isEmpty());}

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) { configureEmptyContent(adapter.isEmpty()); }
        });
        vh.setEmptyText(R.string.no_account);
        vh.list.setAdapter(adapter);
        vh.setOnAddListener(v1 -> onAddAccount());
        viewModel.getAccounts().observe(this, this::onAboutAccountFetched);
        return vh;
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull ListFragmentViewHolder vh) {
        navController = Navigation.findNavController(vh.getRoot());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.account_list_menu, menu);
        onPrepareSearchMenu(menu.findItem(R.id.search_account), R.string.search_account, saveData.queryAccount);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        onFilter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        super.onQueryTextChange(newText);
        if (Check.isEmptyString(newText)){
            onFilter(null);
            return true;
        }
        return false;
    }

    private void onFilter(String key){ adapter.getFilter().filter(key); }

    @Override
    public void onItemChildClicked(AccountsAdapter accountsAdapter, final AccountsAdapter.AccountViewHolder vh, View v) {
        if (null == getContext()) return;
        final Account account = adapter.getItem(vh.getAbsoluteAdapterPosition());
        if (vh.options == v){
            PopupMenu menu = new PopupMenu(getContext(), v);
            menu.inflate(R.menu.account_list_item_options_menu);
            menu.setOnMenuItemClickListener(item -> {
                viewModel.setSelectedAccount(account);
                final int itemId = item.getItemId();
                if (R.id.edit == itemId) {
                    onEditAccount(account);
                    return true;
                }
                else if (R.id.delete == itemId) {
                    onDeleteAccount(account);
                    return true;
                }
                return false;
            });
            menu.show();
        }
        else if (vh.getRoot() == v) {
            onViewTransactions(account);
        }
    }

    private void onAboutAccountFetched(List<AboutAccount> accounts){
        adapter.submitList(accounts);
        adapter.onRestoreData(saveData.adapterSaveData);
    }

    private void onAddAccount(){
        viewModel.setSelectedAccount(null);
        getNavController().navigate(R.id.action_accountsList_to_inputAccount);
    }

    private void onEditAccount(Account account){
        viewModel.setSelectedAccount(account);
        getNavController().navigate(R.id.action_accountsList_to_inputAccount);
    }

    private void onDeleteAccount(final Account account){
        if (null == getContext()) return;
        new AlertDialog.Builder(getContext())
                .setMessage(getResources().getQuantityString(R.plurals.warning_delete_accounts, 1))
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(android.R.string.ok, (di, which) -> {
                    viewModel.setSelectedAccount(null);
                    viewModel.deleteAccount(account);
                })
                .show();
    }

    private void onViewTransactions(Account account) {
        transactionsViewModel.loadTransactionsForAccount(account);
        getNavController().navigate(R.id.action_accountsList_to_transactionsList);
    }

    private static class AccountListSaveData {
        String queryAccount;
        BaseRecyclerViewListAdapterFilterable.BaseRecyclerViewListAdapterFilterableSaveData adapterSaveData;
    }
}
