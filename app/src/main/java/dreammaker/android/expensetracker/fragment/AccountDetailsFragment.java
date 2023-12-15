package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.FragmentAccountDetailsBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.itemdecoration.SimpleEmptyRecyclerViewDecoration;
import dreammaker.android.expensetracker.listener.ChoiceModel;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ResourceUtil;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.AccountViewModel;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionHistoryViewModel;

@SuppressWarnings("unused")
public class AccountDetailsFragment extends Fragment {

    private static final String TAG = AccountDetailsFragment.class.getSimpleName();

    private FragmentAccountDetailsBinding mBinding;

    private NavController navController;

    private AccountViewModel mAccountVM;
    
    private TransactionHistoryViewModel mHistoryVM;

    private ChoiceModel mChoiceModel;

    private AccountModel mAccount;

    public AccountDetailsFragment() {}

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
        mAccountVM = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(AccountViewModel.class);
        mHistoryVM = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TransactionHistoryViewModel.class);
        mAccountVM.setCallbackIfTaskExists(AccountViewModel.DELETE_ACCOUNTS,this,this::onAccountDeleted);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentAccountDetailsBinding.inflate(inflater,container,false);
        mAccountVM.getAccountById(getExtraAccountId()).observe(getViewLifecycleOwner(),this::onAccountFetched);
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
        mBinding.list.addItemDecoration(new SimpleEmptyRecyclerViewDecoration(getText(R.string.label_no_history),
                ResourceUtil.getDrawable(requireContext(),R.drawable.ic_baseline_history_72)));
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setTitle() {
        requireActivity().setTitle(R.string.label_person_details);
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

    private void onClickDeleteAccount() {
        // TODO: highlight the account name
        final long id = mAccount.getId();
        final String name = mAccount.getName();
        String message = getResources().getQuantityString(R.plurals.warning_delete_accounts, 1,name);
        DialogUtil.createMessageDialog(requireContext(),message,
                getText(R.string.yes),(dialog, which) -> deleteAccount(id),
                getText(R.string.no),null,false)
                .show();
    }

    private void onClickEditAccount() {
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_UPDATE);
        args.putLong(Constants.EXTRA_ID,mAccount.getId());
        navController.navigate(R.id.action_account_details_to_input_account,args);
    }

    private void deleteAccount(long id) {
        mAccountVM.removeAccounts(new long[]{id}).observe(this,this::onAccountDeleted);
    }

    private void onAccountDeleted(DBViewModel.AsyncQueryResult result) {
        Integer count = (Integer) result.getResult();
        if (null == count || count != 0) {
            // TODO: show the error message
            ToastUtil.showErrorShort(requireContext(),"");
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