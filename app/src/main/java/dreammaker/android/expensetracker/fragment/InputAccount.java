package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.ActivityModel;
import dreammaker.android.expensetracker.activity.ActivityModelProvider;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.databinding.InputAccountBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.AccountViewModel;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;

@SuppressWarnings("unused")
public class InputAccount extends Fragment {

    private static final String TAG = InputAccount.class.getSimpleName();

    private static final String KEY_ACCOUNT_VALUE_SET = "account_value_set";

    private NavController navController;

    private AccountViewModel mViewModel;

    private InputAccountBinding mBinding;

    private AccountModel mAccount;

    private boolean mAccountSet = false;

    public InputAccount(){
        super();
    }

    private boolean isEditOperation() {
        return Constants.ACTION_UPDATE.equals(requireArguments().getString(Constants.EXTRA_ACTION));
    }

    private long getAccountId() {
        return requireArguments().getLong(Constants.EXTRA_ID,0L);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(AccountViewModel.class);
        if (requireActivity() instanceof ActivityModelProvider) {
            ActivityModel model = ((ActivityModelProvider) requireActivity()).getActivityModel();
            model.addOnBackPressedCallback(this,this::onBackPressed);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = InputAccountBinding.inflate(inflater,container,false);
        if (isEditOperation()) {
            long id = getAccountId();
            mViewModel.getAccountById(id).observe(getViewLifecycleOwner(),this::onAccountFetched);
        }
        mViewModel.setCallbackIfTaskExists(AccountViewModel.SAVE_ACCOUNT,getViewLifecycleOwner(),this::onAccountSaveComplete);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        mBinding.containerBalance.setEndIconOnClickListener(v->onToggleCalculator());
        mBinding.buttonSave.setOnClickListener(v->onClickSave());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mAccountSet = savedInstanceState.getBoolean(KEY_ACCOUNT_VALUE_SET,false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ACCOUNT_VALUE_SET, mAccountSet);
    }

    private void setTitle() {
        CharSequence title = isEditOperation() ? getText(R.string.label_update_account) : getText(R.string.label_insert_account);
        requireActivity().setTitle(title);
    }

    private boolean onBackPressed() {
        if (hasAnyValueChanged()) {
            DialogUtil.createMessageDialog(requireContext(),R.string.warning_not_saved,
                    R.string.label_discard, null,
                    R.string.label_exit, (di,which)->exit(),
                    false).show();
            return true;
        }
        return false;
    }

    private void onAccountFetched(@Nullable AccountModel account) {
        if (null == account) {
            ToastUtil.showErrorShort(requireContext(),R.string.error_account_not_found);
            exit();
            return;
        }
        mAccount = account;
        if (mAccountSet) {
            return;
        }
        mBinding.name.setText(account.getName());
        mBinding.balance.setText(account.getBalance().toString());
        mBinding.containerBalance.setHelperText(TextUtil.currencyToText(requireContext(),account.getBalance()));
        mAccountSet = true;
    }

    private void onClickSave() {
        if (!validate()) {
            return;
        }
        CharSequence name = mBinding.name.getText();
        Currency balance = TextUtil.tryConvertToCurrencyOrNull(mBinding.balance.getText());
        Account account = new Account();
        if (isEditOperation()) {
            if (!hasAnyValueChanged()) {
                ToastUtil.showMessageShort(requireContext(),R.string.message_no_change_no_save);
                exit();
                return;
            }
            account.setId(getAccountId());
        }
        account.setName(name.toString());
        account.setBalance(balance);
        mViewModel.saveAccount(account).observe(getViewLifecycleOwner(),this::onAccountSaveComplete);
    }

    private boolean validate() {
        CharSequence name = mBinding.name.getText();
        CharSequence txtBalance = mBinding.balance.getText();

        mBinding.containerName.setError(null);
        mBinding.containerBalance.setError(null);

        // validate inputs
        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            mBinding.containerName.setError(getString(R.string.error_empty_input));
            valid = false;
        }
        if (TextUtils.isEmpty(txtBalance)) {
            mBinding.containerBalance.setError(getString(R.string.error_empty_input));
            valid = false;
        }
        else {
            Currency balance = TextUtil.tryConvertToCurrencyOrNull(txtBalance);
            valid = null != balance;
            if (!valid) {
                mBinding.containerBalance.setError(getString(R.string.error_invalid_numeric_value));
            }
        }
        return valid;
    }

    private boolean hasAnyValueChanged() {
        CharSequence name = mBinding.name.getText();
        CharSequence txtBalance = mBinding.balance.getText();
        if (isEditOperation()) {
            return mAccountSet && (!TextUtils.equals(name, mAccount.getName())
                    || !mAccount.getBalance().equals(TextUtil.tryConvertToCurrencyOrNull(txtBalance)));
        }
        else {
            return !TextUtils.isEmpty(name) || !TextUtils.isEmpty(txtBalance);
        }
    }

    private void onAccountSaveComplete(@NonNull DBViewModel.AsyncQueryResult result) {
        Account account = (Account) result.getResult();
        if (null == account) {
            Log.e(TAG,"fail to save account",result.getError());
            ToastUtil.showErrorShort(requireContext(),R.string.error_save);
            return;
        }
        ToastUtil.showSuccessShort(requireContext(),R.string.account_save_successful);
        if (isEditOperation()) {
            exit();
        }
        else {
            showAccountDetails(account);
        }
    }

    private void showAccountDetails(Account account) {
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ID,account.getId());
        navController.navigate(R.id.action_input_account_to_account_details,args);
    }

    private void onToggleCalculator() {
        // TODO: show to hide calculator
    }

    private void exit() {
        navController.popBackStack();
    }
}
