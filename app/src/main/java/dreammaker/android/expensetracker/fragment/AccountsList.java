package dreammaker.android.expensetracker.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.ActivityTransactionInput;
import dreammaker.android.expensetracker.collection.FakeList;
import dreammaker.android.expensetracker.database.model.AccountDisplayModel;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.view.adapter.AccountsListAdapter;
import dreammaker.android.expensetracker.viewmodel.AccountsViewModel;
import dreammaker.android.expensetracker.viewmodel.SavedStateViewModel;

public class AccountsList extends Fragment {

    private static final String TAG = "AccountsList";

    private static final String KEY_QUERY_STRING = "query_string";

    private static final int SEARCH_KEY_LENGTH_THRESHOLD = 2;

    private AccountsViewModel viewModel;
    private SavedStateViewModel mSavedState;
    private NavController navController;

    private RecyclerView list;
    private TextView emptyView;
    private FloatingActionButton btnAdd;
    private AccountsListAdapter accountsAdapter;

    private String mQueryString = null;

    public AccountsList() { super(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        requireActivity().setTitle(R.string.label_accounts);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(requireActivity(),new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(AccountsViewModel.class);
        mSavedState = new ViewModelProvider(this).get(SavedStateViewModel.class);
        list = view.findViewById(R.id.list);
        emptyView = view.findViewById(R.id.empty);
        list.setVisibility(View.VISIBLE);
        btnAdd = view.findViewById(R.id.add);
        emptyView.setText(R.string.no_account);
        btnAdd.setOnClickListener(v -> onAddAccount());
        accountsAdapter = new AccountsListAdapter(requireContext());
        accountsAdapter.setOnRecyclerViewItemClickListener((adapter,child,position) -> onClickAccountItem(child,position));
        accountsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                onDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                onDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                onDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                onDataSetChanged();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                onDataSetChanged();
            }
        });
        list.setAdapter(accountsAdapter);
        mQueryString = mSavedState.getString(KEY_QUERY_STRING);
        viewModel.getAllAccountsForDisplay().observe(getViewLifecycleOwner(),this::onAccountsFetched);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSavedState.put(KEY_QUERY_STRING,mQueryString);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // TODO: use different search method
        inflater.inflate(R.menu.account_list_menu, menu);
        SearchView search = (SearchView) menu.findItem(R.id.search_account).getActionView();
        search.setQuery(mQueryString,false);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return true;}

            @Override
            public boolean onQueryTextChange(String newText) {
                onFilter(newText);
                return true;
            }
        });
    }

    private void onFilter(@Nullable String key){
        mQueryString = key;
        if (TextUtils.isEmpty(key)) {
            accountsAdapter.getFilter().filter(null);
        }
        else if (key.length() > SEARCH_KEY_LENGTH_THRESHOLD) {
            accountsAdapter.getFilter().filter(key);
        }
    }

    private void onDataSetChanged() {
        if (accountsAdapter.isEmpty()) {
            list.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            emptyView.setVisibility(View.INVISIBLE);
            list.setVisibility(View.VISIBLE);
        }
    }

    private void onClickAccountItem(View child, int position) {
        AccountDisplayModel account = accountsAdapter.getItem(position);
        int childId = child.getId();
        if (childId == R.id.options){
            PopupMenu menu = new PopupMenu(requireContext(), child);
            menu.inflate(R.menu.account_list_item_options_menu);
            menu.setOnMenuItemClickListener(item -> {
                final int itemId = item.getItemId();
                if (R.id.edit == itemId) {
                    onEditAccount(account);
                    return true;
                }
                else if (R.id.delete == itemId) {
                    onDeleteAccount(account);
                    return true;
                }
                else if (R.id.send_money == itemId) {
                    onSendMoneyFrom(account);
                }
                return false;
            });
            menu.show();
        }
        else {
            onViewTransactions(account);
        }
    }

    private void onAccountsFetched(@Nullable List<AccountDisplayModel> accounts) {
        accountsAdapter.setOriginalItems(accounts);
        accountsAdapter.submitList(accounts);
        onFilter(mQueryString);
    }

    private void onAddAccount(){
        navController.navigate(R.id.action_accountsList_to_inputAccount);
    }

    @Deprecated
    private void onEditAccount(@NonNull AccountDisplayModel account){
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ACCOUNT,account.getId());
        navController.navigate(R.id.action_accountsList_to_inputAccount,args);
    }

    @Deprecated
    private void onDeleteAccount(@NonNull AccountDisplayModel account){
        if (null == getContext()) return;
        new AlertDialog.Builder(getContext())
                .setMessage(getResources().getQuantityString(R.plurals.warning_delete_accounts, 1))
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(android.R.string.ok, (di, which) -> {
                    viewModel.removeMultipleAccounts("delete_account", Arrays.asList(account.getId()), result -> {});
                })
                .show();
    }

    @Deprecated
    private void onSendMoneyFrom(@NonNull AccountDisplayModel account) {
        Intent intent = new Intent(requireContext(), ActivityTransactionInput.class);
        intent.setAction(Constants.ACTION_MONEY_TRANSFER);
        intent.putExtra(Constants.EXTRA_PAYER_ACCOUNT,account.getId());
        startActivity(intent);
    }

    @Deprecated
    private void onViewTransactions(@NonNull AccountDisplayModel account) {
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ACCOUNT,account.getId());
        navController.navigate(R.id.action_accountsList_to_transactionsList,args);
    }
}
