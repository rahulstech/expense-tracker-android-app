package dreammaker.android.expensetracker.fragment;

import android.content.Context;
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
import dreammaker.android.expensetracker.activity.MainActivity;
import dreammaker.android.expensetracker.database.Transaction;
import dreammaker.android.expensetracker.database.TransactionDetails;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.view.OnItemChildClickListener;
import dreammaker.android.expensetracker.view.TransactionsAdapter;
import dreammaker.android.expensetracker.viewmodel.OperationCallback;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

public class TransactionsList extends BaseListFragment<BaseListFragment.ListFragmentViewHolder>
        implements OnItemChildClickListener<TransactionsAdapter, TransactionsAdapter.TransactionsViewHolder> {

    private static final String TAG = "TransactionsList";

    private TransactionsViewModel viewModel;
    private TransactionsAdapter adapter;
    private OperationCallback transactionDeleteCallback = new OperationCallback(){
        @Override
        public void onCompleteDelete(boolean success) {
            if (success) {
                viewModel.reloadWithLastFilterTransactionParameters();
            }
        }
    };

    public TransactionsList(){ super(); }

    public NavController getNavController() { return ((MainActivity) getActivity()).getNavController(); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setFragmentTitle(R.string.label_transactions_list);
        if (Check.isNonNull(getActivity())){
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(TransactionsViewModel.class);
        }
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
        viewModel.getTransactionsPaged().observe(this, this::onTransactionDetailsFetched);
        return viewHolder;
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull ListFragmentViewHolder vh) {
        viewModel.setOperationCallback(transactionDeleteCallback);
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
        final TransactionDetails transaction = adapter.getItem(vh.getAdapterPosition());
        switch (childView.getId()){
            case R.id.options:{
                PopupMenu menu = new PopupMenu(getContext(), childView);
                menu.inflate(R.menu.transaction_list_item_options_menu);
                menu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    switch(itemId){
                        case R.id.edit:{
                            onEditTransaction(transaction);
                            return true;
                        }
                        case R.id.delete:{
                            onDeleteTransaction(transaction);
                            return true;
                        }
                    }
                    return false;
                });
                menu.show();
            }
            break;
            case R.id.account_name:{
                viewModel.loadTransactionsForAccount(transaction.getAccount());
            }
            break;
            case R.id.person_name:{
                viewModel.loadTransactionsForPerson(transaction.getPerson());
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        return viewModel.reset();
    }

    private void onAddTransaction(){
        viewModel.countAccounts(r -> {
            if (r > 0) {
                viewModel.setWorkingTransaction(new Transaction());
                getNavController().navigate(R.id.action_transactionsList_to_inputTransaction);
            }
            else
                MainActivity.showQuickMessage(getActivity(),
                        R.string.message_no_account_for_transaction,
                        android.R.string.ok);
        });
    }

    private void onTransactionDetailsFetched(PagedList<TransactionDetails> transactionDetails){
        adapter.submitList(transactionDetails);
        configureEmptyContent(Check.isNull(transactionDetails) || transactionDetails.isEmpty());
    }

    private void onEditTransaction(Transaction transaction){
        viewModel.setWorkingTransaction(transaction);
        getNavController().navigate(R.id.action_transactionsList_to_inputTransaction);
    }

    private void onDeleteTransaction(final Transaction transaction){
        new AlertDialog.Builder(getContext())
                .setMessage(getResources().getQuantityString(R.plurals.warning_delete_transactions, 1))
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(android.R.string.ok, (di, which) -> viewModel.deleteTransaction(transaction))
                .show();
    }

    private void onFilterTransaction(){ getNavController().navigate(R.id.action_transactionsList_to_filterTransaction); }
}
