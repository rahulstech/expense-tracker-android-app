package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.ActivityTransactionInput;
import dreammaker.android.expensetracker.database.model.TransactionHistoryDisplayModel;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.view.OnItemChildClickListener;
import dreammaker.android.expensetracker.view.TransactionsAdapter;
import dreammaker.android.expensetracker.viewmodel.OperationCallback;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

public class TransactionsList extends BaseListFragment<BaseListFragment.ListFragmentViewHolder>
        implements OnItemChildClickListener<TransactionsAdapter, TransactionsAdapter.TransactionsViewHolder> {

    private static final String TAG = "TransactionsList";

    private TransactionsViewModel viewModel;
    private TransactionsAdapter adapter;
    private NavController navController;
    private OperationCallback transactionDeleteCallback = new OperationCallback(){
        @Override
        public void onCompleteDelete(boolean success) {
            if (success) {
                viewModel.reloadWithLastFilterTransactionParameters();
            }
        }
    };

    public TransactionsList(){ super(); }

    public NavController getNavController() { return navController; }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setFragmentTitle(R.string.label_transactions_list);
        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(TransactionsViewModel.class);
        setHasOptionsMenu(true);
    }

    @NonNull
    @Override
    protected ListFragmentViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        ListFragmentViewHolder viewHolder = new ListFragmentViewHolder(
                inflater.inflate(R.layout.list_content, container, false));
        adapter = new TransactionsAdapter(getContext());
        viewHolder.setEmptyText(R.string.no_transaction);
        viewHolder.setOnAddListener(v1 -> onAddTransaction());
        adapter.setOnItemChildClickListener(this);
        viewHolder.list.setAdapter(adapter);
        return viewHolder;
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull ListFragmentViewHolder vh) {
        navController = Navigation.findNavController(vh.getRoot());
        viewModel.setOperationCallback(transactionDeleteCallback);
        loadTransactions();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.transaction_details_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (R.id.filter == itemId){
            onFilterTransaction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemChildClicked(TransactionsAdapter transactionsAdapter, TransactionsAdapter.TransactionsViewHolder vh, View childView) {
        final TransactionHistoryDisplayModel transaction = adapter.getItem(vh.getAbsoluteAdapterPosition());
        switch (childView.getId()){
            case R.id.options:{
                PopupMenu menu = new PopupMenu(getContext(), childView);
                menu.inflate(R.menu.transaction_list_item_options_menu);
                menu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.delete) {
                        onDeleteTransaction(transaction);
                    }
                    return false;
                });
                menu.show();
            }
            break;
            case R.id.from_account:{
                //viewModel.loadTransactionsForAccount(transaction.getAccount());
            }
            break;
            case R.id.to_account:{
                //viewModel.loadTransactionsForPerson(transaction.getPerson());
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        return viewModel.reset();
    }

    private void loadTransactions() {
        Bundle args = getArguments();
        if (args.containsKey(Constants.EXTRA_ACCOUNT)) {
            viewModel.getTransactionsForAccount(args.getLong(Constants.EXTRA_ACCOUNT)).observe(this,this::onTransactionsFetched);
        }
        else if (args.containsKey(Constants.EXTRA_PERSON)) {
            viewModel.getTransactionsForPerson(args.getLong(Constants.EXTRA_PERSON)).observe(this,this::onTransactionsFetched);
        }
    }

    private void onAddTransaction(){

        Bundle args = getArguments();

        long accountId = args.containsKey(Constants.EXTRA_ACCOUNT) ? args.getLong(Constants.EXTRA_ACCOUNT) : 0;
        long personId = args.containsKey(Constants.EXTRA_PERSON) ? args.getLong(Constants.EXTRA_PERSON) : 0;

        Log.i(TAG,"accountId="+accountId+" personId="+personId);

        Intent intent = new Intent(requireContext(), ActivityTransactionInput.class);
        if (accountId > 0) {
            intent.setAction(Constants.ACTION_INCOME_EXPENSE);
            intent.putExtra(Constants.EXTRA_ACCOUNT,accountId);
        }
        else if (personId > 0) {
            intent.setAction(Constants.ACTION_PAYMENT_DUE);
            intent.putExtra(Constants.EXTRA_PERSON,personId);
        }
        else {
            intent.setAction(Constants.ACTION_PAYMENT_DUE);
        }
        startActivity(intent);
    }

    private void onTransactionsFetched(@Nullable PagedList<TransactionHistoryDisplayModel> transactions){
        adapter.submitList(transactions);
        configureEmptyContent(Check.isNull(transactions) || transactions.isEmpty());
    }

    @Deprecated
    private void onDeleteTransaction(final TransactionHistoryDisplayModel transaction){
        new AlertDialog.Builder(requireContext())
                .setMessage(getResources().getQuantityString(R.plurals.warning_delete_transactions, 1))
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(android.R.string.ok, (di, which) -> {
                    // TODO: delete transaction
                })
                .show();
    }

    private void onFilterTransaction(){ /*getNavController().navigate(R.id.action_transactionsList_to_filterTransaction);*/ }
}
