package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.MainActivity;
import dreammaker.android.expensetracker.database.MoneyTransfer;
import dreammaker.android.expensetracker.database.MoneyTransferDetails;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.view.MoneyTransferHistoryAdapter;
import dreammaker.android.expensetracker.view.OnItemChildClickListener;
import dreammaker.android.expensetracker.viewmodel.MoneyTransferViewModel;

public class MoneyTransferHistory extends BaseListFragment<BaseListFragment.ListFragmentViewHolder>
        implements OnItemChildClickListener<MoneyTransferHistoryAdapter, MoneyTransferHistoryAdapter.MoneyTransferHistoryViewHolder> {

    private MoneyTransferViewModel viewModel;
    private MoneyTransferHistoryAdapter adapter;
    private NavController navController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setFragmentTitle(R.string.label_money_transfer_history);
        if (Check.isNonNull(getActivity())){
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(MoneyTransferViewModel.class);
        }
        setHasOptionsMenu(true);
    }

    @NonNull
    @Override
    protected ListFragmentViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        ListFragmentViewHolder viewHolder = new ListFragmentViewHolder(
                inflater.inflate(R.layout.list_content, container, false));
        adapter = new MoneyTransferHistoryAdapter(getContext());
        viewHolder.setEmptyText(R.string.no_money_transfer_history);
        viewHolder.setOnAddListener(v1 -> onAddMoneyTransfer());
        adapter.setOnItemChildClickListener(this);
        viewHolder.list.setAdapter(adapter);
        viewModel.getMoneyTransferHistories().observe(this, this::onMoneyTransferHistoriesFetched);
        return viewHolder;
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull ListFragmentViewHolder vh) {
        navController = Navigation.findNavController(vh.getRoot());
    }

    @Override
    public boolean onBackPressed() {
        viewModel.setSelectedMoneyTransfer(null);
        return super.onBackPressed();
    }

    @Override
    public void onItemChildClicked(MoneyTransferHistoryAdapter adapter, MoneyTransferHistoryAdapter.MoneyTransferHistoryViewHolder vh, View v) {
        if (null == getContext()) return;
        final MoneyTransferDetails mt = adapter.getItem(vh.getAbsoluteAdapterPosition());
        if (vh.options == v){
            PopupMenu menu = new PopupMenu(getContext(), v);
            menu.inflate(R.menu.account_list_item_options_menu);
            menu.setOnMenuItemClickListener(item -> {
                viewModel.setSelectedMoneyTransfer(mt);
                final int itemId = item.getItemId();
                if (R.id.edit == itemId) {
                    onEditMoneyTransfer(mt);
                    return true;
                }
                else if (R.id.delete == itemId) {
                    onDeleteMoneyTransfer(mt);
                    return true;
                }
                return false;
            });
            menu.show();
        }
    }

    private void onAddMoneyTransfer() {
        viewModel.setSelectedMoneyTransfer(null);
        viewModel.hasAdequateAccounts(has -> {
            if (has)
                navController.navigate(R.id.action_moneyTransferHistory_to_moneyTransfer);
            else
                showQuickMessage(R.string.message_money_transfer_accounts_not_enough);
        });

    }

    private void onEditMoneyTransfer(MoneyTransfer mt) {
        viewModel.setSelectedMoneyTransfer(mt);
        navController.navigate(R.id.action_moneyTransferHistory_to_moneyTransfer);
    }

    private void onDeleteMoneyTransfer(MoneyTransfer mt) {
        new AlertDialog.Builder(getContext())
                .setMessage(getResources().getQuantityString(R.plurals.warning_delete_money_transfer_history, 1))
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(android.R.string.ok, (di, which) -> {
                    viewModel.setSelectedMoneyTransfer(null);
                    viewModel.deleteMoneyTransfer(mt);
                })
                .show();
    }

    private void onMoneyTransferHistoriesFetched(List<MoneyTransferDetails> histories) {
        adapter.submitList(histories);
        configureEmptyContent(null == histories || histories.isEmpty());
    }

    private void showQuickMessage(@StringRes int id) {
        MainActivity.showQuickMessage(getActivity(), id, android.R.string.ok);
    }
}
