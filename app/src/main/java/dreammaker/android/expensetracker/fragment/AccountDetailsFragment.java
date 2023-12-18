package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.adapter.SectionedTransactionHistoryAdapter;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.FragmentAccountDetailsBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.text.SpannableStringUtil;
import dreammaker.android.expensetracker.text.Spans;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.AccountViewModel;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import rahulstech.android.backend.settings.AppSettings;

@SuppressWarnings("unused")
public class AccountDetailsFragment extends BaseEntityWithTransactionHistoriesFragment {

    private static final String TAG = AccountDetailsFragment.class.getSimpleName();

    private FragmentAccountDetailsBinding mBinding;

    private NavController navController;

    private AccountViewModel mAccountVM;

    private AccountModel mAccount;

    private AppSettings mSettings;

    public AccountDetailsFragment() {super();}

    private long getExtraAccountId() {
        return requireArguments().getLong(Constants.EXTRA_ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSettings = AppSettings.get(context);
        mAccountVM = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(AccountViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentAccountDetailsBinding.inflate(inflater,container,false);
        mAccountVM.getAccountById(getExtraAccountId()).observe(getViewLifecycleOwner(),this::onAccountFetched);
        mAccountVM.setCallbackIfTaskExists(AccountViewModel.DELETE_ACCOUNTS,getViewLifecycleOwner(),this::onAccountDeleted);
        loadHistories(ENTITY_ACCOUNTS,getExtraAccountId());
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        mBinding.addMoneyTransfer.setOnClickListener(v->onClickMoneyTransfer());
        mBinding.addExpense.setOnClickListener(v->onClickAddExpense());
        mBinding.addIncome.setOnClickListener(v->onClickAddIncome());
    }

    @Override
    protected RecyclerView getHistoryList() {
        return mBinding.list;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_account_details,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            onClickDeleteAccount();
            return true;
        }
        else if (id == R.id.edit) {
            onClickEditAccount();
            return true;
        }
        else if (id == R.id.group_daily) {
            onClickShowAs(AppSettings.GROUP_DAILY);
            return true;
        }
        else if (id == R.id.group_monthly) {
            onClickShowAs(AppSettings.GROUP_MONTHLY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode,menu);
        updateActionTitle(mode);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        super.onDestroyActionMode(mode);
        updateActionTitle(mode);
    }

    @Override
    public void onItemChecked(@NonNull ActionMode mode, @NonNull View view, int position, boolean checked) {
        super.onItemChecked(mode,view,position,checked);
        updateActionTitle(mode);
    }

    private void setTitle() {
        requireActivity().setTitle(R.string.label_account_details);
    }

    private void updateActionTitle(ActionMode mode) {
        mode.setTitle(getString(R.string.message_selection_count,getHistoryChoiceModel().getCheckedCount()));
    }

    private void onAccountFetched(@Nullable AccountModel account) {
        if (null == account) {
            ToastUtil.showErrorShort(requireContext(),R.string.error_account_not_found);
            exit();
            return;
        }
        mAccount = account;
        mBinding.name.setText(account.getName());
        mBinding.logo.setImageDrawable(getAccountDefaultLogo());
        setBalance(account.getBalance());
    }

    private void setBalance(Currency balance) {
        mBinding.balance.setText(balance.toString());
        if (Currency.ZERO == balance) {
            mBinding.balanceText.setVisibility(View.GONE);
        }
        else {
            mBinding.balanceText.setText(TextUtil.currencyToText(requireContext(),balance));
            mBinding.balanceText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onClickHistory(@NonNull TransactionHistoryModel history) {
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ID,history.getId());
        navController.navigate(R.id.action_account_details_to_history_details,args);
    }

    private void onClickDeleteAccount() {
        final long id = mAccount.getId();
        final String name = mAccount.getName();
        CharSequence message = getResources().getQuantityString(R.plurals.warning_delete_accounts, 1);
        CharSequence highlighted = new SpannableStringUtil(message).append("\n\n")
                .append(name, new Object[]{Spans.bold(),Spans.relativeSize(2)}).toSpannableString();
        DialogUtil.createMessageDialog(requireContext(),highlighted,
                        getText(R.string.no),null,
                        getText(R.string.yes),(dialog, which) -> deleteAccount(id), true)
                .show();
    }

    private void onClickEditAccount() {
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_UPDATE);
        args.putLong(Constants.EXTRA_ID,mAccount.getId());
        navController.navigate(R.id.action_account_details_to_input_account,args);
    }

    private void onClickShowAs(int groupBy) {
        changeHistoryGrouping(groupBy);
    }

    private void deleteAccount(long id) {
        mAccountVM.removeAccounts(new long[]{id}).observe(this,this::onAccountDeleted);
    }

    private void onAccountDeleted(DBViewModel.AsyncQueryResult result) {
        Boolean success = (Boolean) result.getResult();
        if (null == success || !success ) {
            ToastUtil.showErrorShort(requireContext(),getResources().getQuantityString(R.plurals.error_delete_accounts,1));
            if (BuildConfig.DEBUG) {
                Log.e(TAG,"fail to remove account with id="+getExtraAccountId(),result.getError());
            }
        }
    }

    private void onClickMoneyTransfer() {
        navigateToInputTransaction(TransactionType.MONEY_TRANSFER,true);
    }

    private void onClickAddExpense() {
        navigateToInputTransaction(TransactionType.EXPENSE,true);
    }

    private void onClickAddIncome() {
        navigateToInputTransaction(TransactionType.INCOME,false);
    }

    private void navigateToInputTransaction(TransactionType type, boolean payer) {
        final long id = mAccount.getId();
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_INSERT);
        args.putString(Constants.EXTRA_TRANSACTION_TYPE,type.name());
        if (payer) {
            args.putLong(Constants.EXTRA_PAYER_ACCOUNT,id);
        }
        else {
            args.putLong(Constants.EXTRA_PAYEE_ACCOUNT,id);
        }
        navController.navigate(R.id.action_account_details_to_input_history,args);
    }

    private Drawable getAccountDefaultLogo() {
        String name = mAccount.getName();
        return DrawableUtil.getAccountDefaultLogo(name);
    }

    private void exit() {
        navController.popBackStack();
    }
}